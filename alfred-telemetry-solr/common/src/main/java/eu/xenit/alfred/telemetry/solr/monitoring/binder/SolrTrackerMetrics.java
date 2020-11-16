package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.Set;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.alfresco.solr.TrackerState;
import org.alfresco.solr.tracker.AclTracker;
import org.alfresco.solr.tracker.MetadataTracker;
import org.alfresco.solr.tracker.TrackerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrTrackerMetrics implements MeterBinder {

    AlfrescoCoreAdminHandler coreAdminHandler;
    MeterRegistry registry;

    Logger logger = LoggerFactory.getLogger(SolrMetrics.class);

    public SolrTrackerMetrics(AlfrescoCoreAdminHandler coreAdminHandler) {
        this.coreAdminHandler = coreAdminHandler;
    }


    private void registerTrackerMetrics() {
        logger.info("Registering tracker metrics");
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

        Set<String> coreNames = trackerRegistry.getCoreNames();
        for (String coreName : coreNames) {
            Tags tags = Tags.of("core", coreName, "feature", "Approx transactions remaining");
            TrackerRegistry finalTrackerRegistry = trackerRegistry;
            Gauge.builder("alfresco_nodes", trackerRegistry,
                    x -> getTransactionsRemaining(finalTrackerRegistry, coreName))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, "feature", "TX lag");
            Gauge.builder("alfresco_nodes", trackerRegistry, x -> getTxLag(finalTrackerRegistry, coreName))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, "feature", "Last Index TX Commit Time");
            Gauge.builder("alfresco_nodes", trackerRegistry,
                    x -> getLastIndexTxCommitTime(finalTrackerRegistry, coreName))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, "feature", "Approx change sets remaining");
            Gauge.builder("alfresco_acls", trackerRegistry, x -> getChangeSetsRemaining(finalTrackerRegistry, coreName))
                    .tags(tags).register(registry);

            tags = Tags.of("core", coreName, "feature", "Change Set Lag");
            Gauge.builder("alfresco_acls", trackerRegistry, x -> getChangeSetsLag(finalTrackerRegistry, coreName))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, "feature", "Last Index Change Set Commit Time");
            Gauge.builder("alfresco_acls", trackerRegistry,
                    x -> getLastIndexChangeSetCommitTime(finalTrackerRegistry, coreName))
                    .tags(tags)
                    .register(registry);
        }
    }

    private long getLastIndexChangeSetCommitTime(TrackerRegistry trackerRegistry, String coreName) {
        return trackerRegistry.getTrackerForCore(coreName, AclTracker.class).getTrackerState()
                .getLastIndexedChangeSetCommitTime();
    }

    private long getChangeSetsLag(TrackerRegistry trackerRegistry, String coreName) {
        TrackerState aclTrkrState = trackerRegistry.getTrackerForCore(coreName, AclTracker.class).getTrackerState();
        long lastIndexChangeSetCommitTime = aclTrkrState.getLastIndexedChangeSetCommitTime();
        long lastChangeSetCommitTimeOnServer = aclTrkrState.getLastChangeSetCommitTimeOnServer();
        long changeSetLagSeconds = (lastChangeSetCommitTimeOnServer - lastIndexChangeSetCommitTime) / 1000;
        return ((changeSetLagSeconds < 0) ? 0 : changeSetLagSeconds);
    }

    private long getChangeSetsRemaining(TrackerRegistry trackerRegistry, String coreName) {
        TrackerState aclTrkrState = trackerRegistry.getTrackerForCore(coreName, AclTracker.class).getTrackerState();
        long lastIndexedChangeSetId = aclTrkrState.getLastIndexedChangeSetId();
        long lastChangeSetIdOnServer = aclTrkrState.getLastChangeSetIdOnServer();
        long changeSetsToDo = lastChangeSetIdOnServer - lastIndexedChangeSetId;
        return ((changeSetsToDo < 0) ? 0 : changeSetsToDo);
    }

    private long getLastIndexTxCommitTime(TrackerRegistry trackerRegistry, String coreName) {
        return trackerRegistry.getTrackerForCore(coreName, MetadataTracker.class).getTrackerState()
                .getLastIndexedTxCommitTime();
    }

    private long getTxLag(TrackerRegistry trackerRegistry, String coreName) {
        TrackerState metadataTrkrState = trackerRegistry.getTrackerForCore(coreName, MetadataTracker.class)
                .getTrackerState();
        long lastTxCommitTimeOnServer = metadataTrkrState.getLastTxCommitTimeOnServer();
        long lastIndexTxCommitTime = metadataTrkrState.getLastIndexedTxCommitTime();

        long txLagSeconds = (lastTxCommitTimeOnServer - lastIndexTxCommitTime) / 1000;
        return ((txLagSeconds < 0) ? 0 : txLagSeconds);
    }

    private long getTransactionsRemaining(TrackerRegistry trackerRegistry, String coreName) {
        TrackerState metadataTrkrState = trackerRegistry.getTrackerForCore(coreName, MetadataTracker.class)
                .getTrackerState();
        long lastIndexedTxId = metadataTrkrState.getLastIndexedTxId();
        long lastTxIdOnServer = metadataTrkrState.getLastTxIdOnServer();
        long transactionsToDo = lastTxIdOnServer - lastIndexedTxId;
        return (transactionsToDo < 0 ? 0 : transactionsToDo);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        registerTrackerMetrics();
    }
}
