package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.ReplicationHandler;
import org.apache.solr.request.SolrRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrSnapshootMetrics implements MeterBinder {

    private AlfrescoCoreAdminHandler coreAdminHandler;
    private MeterRegistry registry;

    private static final Logger logger = LoggerFactory.getLogger(SolrSnapshootMetrics.class);
    private static final Map<String,String> statusMapping = Map.of(
            "success", "1",
            "failed", "0"
    );
    private static final SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'UTC' YYYY");
    static {
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public SolrSnapshootMetrics(AlfrescoCoreAdminHandler coreAdminHandler) {
        this.coreAdminHandler = coreAdminHandler;
    }

    private void registerSnapshootMetrics() {
        logger.info("Registering snapshoot metrics");
        SolrCore core = coreAdminHandler.getCoreContainer().getCore("alfresco");
        SolrRequestHandler handler = core.getRequestHandler(ReplicationHandler.PATH);
        ReplicationHandler replication = (ReplicationHandler) handler;
        Field snapField = null;
        Tags  tags = Tags.of("core", core.getName());
        Gauge.builder("snapshot.start", replication, x -> getValueFromReport(replication, "startTime"))
                .tags(tags)
                .register(registry);
        Gauge.builder("snapshot.completed", replication, x -> getValueFromReport(replication, "snapshotCompletedAt"))
                .tags(tags)
                .register(registry);
        Gauge.builder("snapshot.file.count", replication, x -> getValueFromReport(replication, "fileCount"))
                .tags(tags)
                .register(registry);
        Gauge.builder("snapshot.status", replication, x -> getValueFromReport(replication, "status"))
                .tags(tags)
                .register(registry);

    }

    private long getValueFromReport(ReplicationHandler replication, String key) {
        Field snapField = null;
        try {
            snapField = ReplicationHandler.class.getDeclaredField("snapShootDetails");
        } catch (NoSuchFieldException e) {
            logger.error("No snapShootDetails field in the ReplicationHandler",e);
        }
        snapField.setAccessible(true);
        NamedList<?> snapValue = null;
        try {
            snapValue = (NamedList<?>) snapField.get(replication);
        } catch (IllegalAccessException e) {
            logger.info("No backup taken yet",e);
        }
        if(snapValue==null)
            return -1;

        Object value = snapValue.get(key);
        if(value instanceof Integer)
            return Long.valueOf((Integer)value).longValue();
        if("status".equals(key))
            return Long.valueOf(statusMapping.get(value)).longValue();
        if("startTime".equals(key) || "snapshotCompletedAt".equals(key)) {
            try {
                return formatter.parse((String)value).getTime();
            } catch (ParseException e) {
                logger.error("Start time or completed time of snapshoot not in correct format",e);
            }
        }

        return -1;
    }


    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        registerSnapshootMetrics();
    }
}
