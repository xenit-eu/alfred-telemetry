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
            logger.error("Solr did not start tracking yet, waiting 10sec");
            try {
                Thread.currentThread().sleep(10_000);
                trackerRegistry = coreAdminHandler.getTrackerRegistry();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Set<String> coreNames = trackerRegistry.getCoreNames();
        for (String coreName : coreNames) {
            TrackerState metadataTrackerState = trackerRegistry.getTrackerForCore(coreName, MetadataTracker.class)
                    .getTrackerState();
            TrackerState aclsTrackerState = trackerRegistry.getTrackerForCore(coreName, AclTracker.class)
                    .getTrackerState();

            // technically these metrics are not per core, but in order to filter in grafana the core is added as a tag
            Tags tags = Tags.of("core", coreName, "state", "Remaining");
            Gauge.builder("alfresco.transactions.nodes", metadataTrackerState,
                    x -> getTransactionsRemaining(metadataTrackerState))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName);
            Gauge.builder("alfresco.transactions.nodes.lag", metadataTrackerState, x -> getTxLag(metadataTrackerState))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName);
            Gauge.builder("alfresco.transactions.nodes.lastIndexCommitTime", metadataTrackerState,
                    x -> getLastIndexTxCommitTime(metadataTrackerState))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, "state", "Remaining");
            Gauge.builder("alfresco.transactions.acls", aclsTrackerState, x -> getChangeSetsRemaining(aclsTrackerState))
                    .tags(tags).register(registry);

            tags = Tags.of("core", coreName);
            Gauge.builder("alfresco.transactions.acls.lag", aclsTrackerState, x -> getChangeSetsLag(aclsTrackerState))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName);
            Gauge.builder("alfresco.transactions.acls.lastIndexCommitTime", aclsTrackerState,
                    x -> getLastIndexChangeSetCommitTime(aclsTrackerState))
                    .tags(tags)
                    .register(registry);
        }
    }

    private long getLastIndexChangeSetCommitTime(TrackerState aclsTrackerState) {
        return aclsTrackerState.getLastIndexedChangeSetCommitTime();
    }

    private long getChangeSetsLag(TrackerState aclsTrackerState) {
        long lastIndexChangeSetCommitTime = aclsTrackerState.getLastIndexedChangeSetCommitTime();
        long lastChangeSetCommitTimeOnServer = aclsTrackerState.getLastChangeSetCommitTimeOnServer();
        long changeSetLagSeconds = (lastChangeSetCommitTimeOnServer - lastIndexChangeSetCommitTime) / 1000;
        return ((changeSetLagSeconds < 0) ? 0 : changeSetLagSeconds);
    }

    private long getChangeSetsRemaining(TrackerState aclsTrackerState) {
        long lastIndexedChangeSetId = aclsTrackerState.getLastIndexedChangeSetId();
        long lastChangeSetIdOnServer = aclsTrackerState.getLastChangeSetIdOnServer();
        long changeSetsToDo = lastChangeSetIdOnServer - lastIndexedChangeSetId;
        return ((changeSetsToDo < 0) ? 0 : changeSetsToDo);
    }

    private long getLastIndexTxCommitTime(TrackerState metadataTrackerState) {
        return metadataTrackerState.getLastIndexedTxCommitTime();
    }

    private long getTxLag(TrackerState metadataTrackerState) {
        long lastTxCommitTimeOnServer = metadataTrackerState.getLastTxCommitTimeOnServer();
        long lastIndexTxCommitTime = metadataTrackerState.getLastIndexedTxCommitTime();

        long txLagSeconds = (lastTxCommitTimeOnServer - lastIndexTxCommitTime) / 1000;
        return ((txLagSeconds < 0) ? 0 : txLagSeconds);
    }

    private long getTransactionsRemaining(TrackerState metadataTrackerState) {
        long lastIndexedTxId = metadataTrackerState.getLastIndexedTxId();
        long lastTxIdOnServer = metadataTrackerState.getLastTxIdOnServer();
        long transactionsToDo = lastTxIdOnServer - lastIndexedTxId;
        return (transactionsToDo < 0 ? 0 : transactionsToDo);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        registerTrackerMetrics();
    }
}
