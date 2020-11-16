package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.alfresco.solr.SolrInformationServer;
import org.alfresco.solr.tracker.TrackerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrCoreStatsMetrics implements MeterBinder {

    AlfrescoCoreAdminHandler coreAdminHandler;
    MeterRegistry registry;

    ArrayList fieldsToMonitorNodes = new ArrayList(Arrays.asList(
            "Alfresco Nodes in Index",
            "Alfresco Transactions in Index",
            "Alfresco Stated in Index",
            "Alfresco Unindexed Nodes",
            "Alfresco Error Nodes in Index"));
    ArrayList fieldsToMonitorAcls = new ArrayList(Arrays.asList(
            "Alfresco Acls in Index",
            "Alfresco Acl Transactions in Index"));

    Logger logger = LoggerFactory.getLogger(SolrCoreStatsMetrics.class);

    public SolrCoreStatsMetrics(AlfrescoCoreAdminHandler coreAdminHandler) {
        this.coreAdminHandler = coreAdminHandler;
    }

    private void registerCoreStats() {
        TrackerRegistry trackerRegistry = coreAdminHandler.getTrackerRegistry();

        while (trackerRegistry.getCoreNames().size() == 0) {
            logger.error("Solr did not start tracking yet, waiting 30sec");
            try {
                Thread.currentThread().sleep(30_000);
                trackerRegistry = coreAdminHandler.getTrackerRegistry();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (String coreName : trackerRegistry.getCoreNames()) {
            SolrInformationServer server = (SolrInformationServer) coreAdminHandler.getInformationServers()
                    .get(coreName);
            Iterable<Entry<String, Object>> stats = null;
            try {
                stats = server.getCoreStats();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
            for (Entry<String, Object> stat : stats) {
                Entry<String, Object> key = stat;
                Tags tags = Tags.of("core", coreName, "feature", stat.getKey());
                if (fieldsToMonitorNodes.contains(stat.getKey())) {
                    Gauge.builder("alfresco.nodes", server, x -> getValueFromServer(server, stat.getKey()))
                            .tags(tags)
                            .register(registry);
                }
                if (fieldsToMonitorAcls.contains(stat.getKey())) {
                    Gauge.builder("alfresco.acls", server, x -> getValueFromServer(server, stat.getKey())).tags(tags)
                            .register(registry);
                }
            }
        }
    }

    private Double getValueFromServer(SolrInformationServer x, String key) {
        try {
            for (Entry<String, Object> stat : x.getCoreStats()) {
                if (stat.getKey().equals(key)) {
                    return Double.parseDouble(stat.getValue().toString());
                }
            }
        } catch (IOException e) {
            logger.error("Cannot get coreStats");
        }
        return null;
    }


    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        registerCoreStats();
    }
}
