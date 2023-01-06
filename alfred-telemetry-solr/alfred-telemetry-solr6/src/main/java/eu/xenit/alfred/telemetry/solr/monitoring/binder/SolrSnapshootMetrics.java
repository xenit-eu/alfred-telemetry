package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
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

public class SolrSnapshootMetrics implements MeterBinder {
    public static final String CMD_DETAILS = "details";
    static final String COMMAND = "command";
    public static final String CMD_BACKUP = "backup";

    private AlfrescoCoreAdminHandler coreAdminHandler;
    private MeterRegistry registry;

    private static final Logger logger = LoggerFactory.getLogger(SolrSnapshootMetrics.class);
    private static final Map<String, Long> statusMapping = Map.of("success", 1L, "failed", 0L);
    private SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'UTC' yyyy");

    public SolrSnapshootMetrics(AlfrescoCoreAdminHandler coreAdminHandler) {
        this.coreAdminHandler = coreAdminHandler;
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private void registerSnapshootMetrics() throws Exception {
        logger.info("Registering snapshoot metrics");
        SolrCore core = coreAdminHandler.getCoreContainer().getCore("alfresco");
        SolrRequestHandler handler = core.getRequestHandler(ReplicationHandler.PATH);
        RequestHandlerBase replication = (RequestHandlerBase) handler;
        Tags tags = Tags.of("core", core.getName());
        NamedList<?> snapshotStats = getSnapshotStats(replication, core);
        if (snapshotStats != null) {
            buildGauge(replication, tags, snapshotStats, "snapshot.start", "startTime");
            buildGauge(replication, tags, snapshotStats, "snapshot.completed", "snapshotCompletedAt");
            buildGauge(replication, tags, snapshotStats, "snapshot.file.count", "fileCount");
            buildGauge(replication, tags, snapshotStats, "snapshot.status", "status");
        }
    }

    private NamedList<?> getSnapshotStats(RequestHandlerBase replication, SolrCore core) throws Exception {
        SolrParams solrParams = new MapSolrParams(Map.of(COMMAND, CMD_DETAILS));
        SolrQueryResponse solrQueryResponse = new SolrQueryResponse();
        replication.handleRequestBody(new LocalSolrQueryRequest(core, solrParams), solrQueryResponse);
        NamedList<?> values = solrQueryResponse.getValues();
        if (values == null) return null;
        NamedList<?> details = (NamedList<?>) values.get(CMD_DETAILS);
        if (details == null) return null;
        return (NamedList<?>) details.get(CMD_BACKUP);
    }

    private long getValueFromReport(NamedList<?> snapshotStats, String key) {
        Object value = snapshotStats.get(key);
        if (value instanceof Integer) return ((Integer) value).longValue();
        if ("status".equals(key)) return statusMapping.get(value);
        if ("startTime".equals(key) || "snapshotCompletedAt".equals(key)) {
            try {
                return formatter.parse((String) value).getTime();
            } catch (ParseException e) {
                logger.error("Start time or completed time of snapshoot not in correct format", e);
            }
        }
        return -1;
    }

    private void buildGauge(RequestHandlerBase replication, Tags tags, NamedList<?> snapshotStats, String name, String key) {
        Gauge.builder(name, replication, x -> getValueFromReport(snapshotStats, key)).tags(tags).register(registry);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        try {
            registerSnapshootMetrics();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
