package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.io.IOException;
import java.util.Map.Entry;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.alfresco.solr.SolrInformationServer;
import org.alfresco.solr.tracker.TrackerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrCoreStatsMetrics implements MeterBinder {

    private static final String METER_ALFRESCO_NODES = "alfresco.nodes";
    private static final String TAG_STATE = "state";
    private static final String TAG_VALUE_INDEXED = "Indexed";

    private final AlfrescoCoreAdminHandler coreAdminHandler;
    private MeterRegistry registry;

    private static final Logger logger = LoggerFactory.getLogger(SolrCoreStatsMetrics.class);

    public SolrCoreStatsMetrics(AlfrescoCoreAdminHandler coreAdminHandler) {
        this.coreAdminHandler = coreAdminHandler;
    }

    private void registerCoreStats() {
        TrackerRegistry trackerRegistry = coreAdminHandler.getTrackerRegistry();

        while (trackerRegistry.getCoreNames().size() == 0) {
            logger.error("Solr did not start tracking yet, waiting 10sec");
            try {
                Thread.currentThread().sleep(10_000);
                trackerRegistry = coreAdminHandler.getTrackerRegistry();
            } catch (InterruptedException e) {
                logger.error("Fail to wait 10 sec", e);
            }
        }

        for (String coreName : trackerRegistry.getCoreNames()) {
            SolrInformationServer server = (SolrInformationServer) coreAdminHandler.getInformationServers()
                    .get(coreName);

            Tags tags = Tags.of("core", coreName, TAG_STATE, TAG_VALUE_INDEXED);
            Gauge.builder(METER_ALFRESCO_NODES, server,
                    x -> getCoreStat(server, "Alfresco Nodes in Index"))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, TAG_STATE, "Unindexed");
            Gauge.builder(METER_ALFRESCO_NODES, server,
                    x -> getCoreStat(server, "Alfresco Unindexed Nodes"))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, TAG_STATE, "Error");
            Gauge.builder(METER_ALFRESCO_NODES, trackerRegistry,
                    x -> getCoreStat(server, "Alfresco Error Nodes in Index"))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, TAG_STATE, TAG_VALUE_INDEXED);
            Gauge.builder("alfresco.acls", trackerRegistry,
                    x -> getCoreStat(server, "Alfresco Acls in Index"))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, TAG_STATE, "States");
            Gauge.builder("alfresco.states", trackerRegistry,
                    x -> getCoreStat(server, "Alfresco States in Index"))
                    .tags(tags)
                    .register(registry);

            // technically these metrics are not per core, but in order to filter in grafana the core is added as a tag
            tags = Tags.of("core", coreName, TAG_STATE, TAG_VALUE_INDEXED);
            Gauge.builder("alfresco.transactions.nodes", trackerRegistry,
                    x -> getCoreStat(server, "Alfresco Transactions in Index"))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, TAG_STATE, TAG_VALUE_INDEXED);
            Gauge.builder("alfresco.transactions.acls", trackerRegistry,
                    x -> getCoreStat(server, "Alfresco Acl Transactions in Index"))
                    .tags(tags)
                    .register(registry);

        }
    }


    private long getCoreStat(SolrInformationServer server, String key) {
        try {
            for (Entry<String, Object> entry : server.getCoreStats()) {
                if (key.equals(entry.getKey())) {
                    return Long.parseLong(entry.getValue().toString());
                }
            }
        } catch (IOException e) {
            logger.error("Error getting core stats");
        }
        return -1;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        registerCoreStats();
    }
}
