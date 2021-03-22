package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.Map.Entry;
import java.util.Set;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.alfresco.solr.SolrInformationServer;
import org.alfresco.solr.tracker.TrackerRegistry;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrFTSMetrics implements MeterBinder {

    private AlfrescoCoreAdminHandler coreAdminHandler;
    private MeterRegistry registry;

    private static final Logger logger = LoggerFactory.getLogger(SolrFTSMetrics.class);

    public SolrFTSMetrics(AlfrescoCoreAdminHandler coreAdminHandler) {
        this.coreAdminHandler = coreAdminHandler;
    }

    private void registerFTSMetrics() {
        logger.info("Registering FTS metrics");
        TrackerRegistry trackerRegistry = coreAdminHandler.getTrackerRegistry();

        while (trackerRegistry.getCoreNames().size() == 0) {
            logger.error("Solr did not start tracking yet, waiting 10sec");
            try {
                Thread.currentThread().sleep(10_000);
                trackerRegistry = coreAdminHandler.getTrackerRegistry();
            } catch (InterruptedException e) {
                logger.error("Fail to wait 10 sec ",e);
            }
        }

        Set<String> coreNames = coreAdminHandler.getTrackerRegistry().getCoreNames();
        for (String coreName : coreNames) {
            NamedList<Object> report = new NamedList();
            SolrInformationServer server = (SolrInformationServer) coreAdminHandler.getInformationServers()
                    .get(coreName);
            server.addFTSStatusCounts(report);

            Tags tags = Tags.of("core", coreName, "state", "Clean");
            Gauge.builder("alfresco.fts", server, x -> getValueFromReport(server, "Node count with FTSStatus Clean"))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, "state", "Dirty");
            Gauge.builder("alfresco.fts", server, x -> getValueFromReport(server, "Node count with FTSStatus Dirty"))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, "state", "New");
            Gauge.builder("alfresco.fts", server, x -> getValueFromReport(server, "Node count with FTSStatus New"))
                    .tags(tags)
                    .register(registry);

        }
    }

    private long getValueFromReport(SolrInformationServer server, String key) {
        NamedList<Object> report = new NamedList();
        server.addFTSStatusCounts(report);
        for (Entry fts : report) {
            if (fts.getKey().equals(key)) {
                return Long.parseLong(fts.getValue().toString());
            }
        }
        return -1;
    }


    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        registerFTSMetrics();
    }
}
