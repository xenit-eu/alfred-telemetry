package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.ReplicationHandler;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.SolrQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

public class SolrSnapshotMetrics implements MeterBinder {
    public static final String CMD_DETAILS = "details";
    static final String COMMAND = "command";
    public static final String CMD_BACKUP = "backup";

    private final AlfrescoCoreAdminHandler coreAdminHandler;
    private MeterRegistry registry;

    private static final Logger logger = LoggerFactory.getLogger(SolrSnapshotMetrics.class);
    private static final Map<String, Long> statusMapping = Map.of("success", 1L, "failed", 0L);
    private final SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'UTC' yyyy");

    public SolrSnapshotMetrics(AlfrescoCoreAdminHandler coreAdminHandler) {
        this.coreAdminHandler = coreAdminHandler;
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private void registerSnapshotMetrics() {
        logger.info("Registering snapshot metrics");
        SolrCore core = coreAdminHandler.getCoreContainer().getCore("alfresco");
        SolrRequestHandler handler = core.getRequestHandler(ReplicationHandler.PATH);
        RequestHandlerBase replication = (RequestHandlerBase) handler;
        Tags tags = Tags.of("core", core.getName());
        buildGauge(replication, tags, core, "snapshot.start", "startTime");
        buildGauge(replication, tags, core, "snapshot.completed", "snapshotCompletedAt");
        buildGauge(replication, tags, core, "snapshot.completed", "fileCount");
        buildGauge(replication, tags, core, "snapshot.status", "status");
    }

    private NamedList<?> getSnapshotStats(RequestHandlerBase replication, SolrCore core) {
        SolrQueryResponse solrQueryResponse = new SolrQueryResponse();
        try {
            replication.handleRequestBody(new LocalSolrQueryRequest(core,
                    new MapSolrParams(Map.of(COMMAND, CMD_DETAILS))), solrQueryResponse);
        } catch (Exception e) {
            logger.error("Failed to fetch SnapShot Details", e);
        }
        NamedList<?> values = solrQueryResponse.getValues();
        if (values == null) return null;
        NamedList<?> details = (NamedList<?>) values.get(CMD_DETAILS);
        if (details == null) return null;
        return (NamedList<?>) details.get(CMD_BACKUP);
    }

    private long getValueFromReport(RequestHandlerBase replication, SolrCore core, String key) {
        NamedList<?> snapshotStats = getSnapshotStats(replication, core);
        if (snapshotStats == null) return -1;
        Object value = snapshotStats.get(key);
        if (value instanceof Integer) return ((Integer) value).longValue();
        if ("status".equals(key)) return statusMapping.get(value);
        if ("startTime".equals(key) || "snapshotCompletedAt".equals(key)) {
            try {
                return formatter.parse((String) value).getTime();
            } catch (ParseException e) {
                logger.error("Start time or completed time of snapshot not in correct format", e);
            }
        }
        return -1;
    }

    private void buildGauge(RequestHandlerBase replication, Tags tags, SolrCore core, String name, String key) {
        Gauge.builder(name, replication, x -> getValueFromReport(replication, core, key)).tags(tags).register(registry);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        registerSnapshotMetrics();
    }
}
