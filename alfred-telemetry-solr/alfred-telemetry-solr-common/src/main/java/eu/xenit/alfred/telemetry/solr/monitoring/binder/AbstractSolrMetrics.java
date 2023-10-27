package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.alfresco.solr.tracker.TrackerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSolrMetrics implements MeterBinder {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSolrMetrics.class);
    protected final AlfrescoCoreAdminHandler coreAdminHandler;
    protected MeterRegistry registry;

    AbstractSolrMetrics(AlfrescoCoreAdminHandler coreAdminHandler) {
        this.coreAdminHandler = coreAdminHandler;
    }

    protected TrackerRegistry getTrackerRegistryWhenAvailable() {
        logger.info("Registering tracker metrics");
        TrackerRegistry trackerRegistry = coreAdminHandler.getTrackerRegistry();

        while (trackerRegistry.getCoreNames().isEmpty()) {
            logger.error("Solr did not start tracking yet, waiting 10sec");
            try {
                Thread.currentThread().sleep(10_000);
                trackerRegistry = coreAdminHandler.getTrackerRegistry();
            } catch (InterruptedException e) {
                logger.error("Fail to wait 10 sec", e);
            }
        }
        return trackerRegistry;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        registerMetrics();
    }

    protected abstract void registerMetrics();
}
