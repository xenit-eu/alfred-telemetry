package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.alfresco.solr.SolrInformationServer;
import org.alfresco.solr.tracker.TrackerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map.Entry;

public class SolrCoreStatsMetrics extends AbstractSolrMetrics implements MeterBinder {

    private static final String METER_ALFRESCO_NODES = "alfresco.nodes";
    private static final String TAG_STATE = "state";
    private static final String TAG_VALUE_INDEXED = "Indexed";


    private static final Logger logger = LoggerFactory.getLogger(SolrCoreStatsMetrics.class);

    public SolrCoreStatsMetrics(AlfrescoCoreAdminHandler coreAdminHandler) {
        super(coreAdminHandler);
    }

    @Override
    protected void registerMetrics() {
        TrackerRegistry trackerRegistry = getTrackerRegistryWhenAvailable();

        for (String coreName : trackerRegistry.getCoreNames()) {
            Tags tags = Tags.of("core", coreName, TAG_STATE, TAG_VALUE_INDEXED);
            Gauge.builder(METER_ALFRESCO_NODES, coreAdminHandler,
                            x -> getCoreStat(x, coreName, "Alfresco Nodes in Index"))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, TAG_STATE, "Unindexed");
            Gauge.builder(METER_ALFRESCO_NODES, coreAdminHandler,
                            x -> getCoreStat(x, coreName, "Alfresco Unindexed Nodes"))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, TAG_STATE, "Error");
            Gauge.builder(METER_ALFRESCO_NODES, coreAdminHandler,
                            x -> getCoreStat(x, coreName, "Alfresco Error Nodes in Index"))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, TAG_STATE, TAG_VALUE_INDEXED);
            Gauge.builder("alfresco.acls", coreAdminHandler,
                            x -> getCoreStat(x, coreName, "Alfresco Acls in Index"))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, TAG_STATE, "States");
            Gauge.builder("alfresco.states", coreAdminHandler,
                            x -> getCoreStat(x, coreName, "Alfresco States in Index"))
                    .tags(tags)
                    .register(registry);

            // technically these metrics are not per core, but in order to filter in grafana the core is added as a tag
            tags = Tags.of("core", coreName, TAG_STATE, TAG_VALUE_INDEXED);
            Gauge.builder("alfresco.transactions.nodes", coreAdminHandler,
                            x -> getCoreStat(x, coreName, "Alfresco Transactions in Index"))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, TAG_STATE, TAG_VALUE_INDEXED);
            Gauge.builder("alfresco.transactions.acls", coreAdminHandler,
                            x -> getCoreStat(x, coreName, "Alfresco Acl Transactions in Index"))
                    .tags(tags)
                    .register(registry);

        }
    }


    private static long getCoreStat(AlfrescoCoreAdminHandler coreAdminHandler, String coreName, String key) {
        SolrInformationServer server = (SolrInformationServer) coreAdminHandler.getInformationServers()
                .get(coreName);
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

}
