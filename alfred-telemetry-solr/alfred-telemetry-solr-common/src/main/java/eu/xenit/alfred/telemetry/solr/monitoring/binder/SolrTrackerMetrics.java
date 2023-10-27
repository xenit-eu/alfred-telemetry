package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.alfresco.solr.TrackerState;
import org.alfresco.solr.tracker.AclTracker;
import org.alfresco.solr.tracker.MetadataTracker;
import org.alfresco.solr.tracker.TrackerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class SolrTrackerMetrics extends AbstractSolrMetrics implements MeterBinder {

    private static final Logger logger = LoggerFactory.getLogger(SolrTrackerMetrics.class);

    public SolrTrackerMetrics(AlfrescoCoreAdminHandler coreAdminHandler) {
        super(coreAdminHandler);
    }

    @Override
    protected void registerMetrics() {
        TrackerRegistry trackerRegistry = getTrackerRegistryWhenAvailable();

        Set<String> coreNames = trackerRegistry.getCoreNames();
        for (String coreName : coreNames) {
            if (trackerRegistry.getTrackerForCore(coreName, MetadataTracker.class) == null || trackerRegistry.getTrackerForCore(coreName, AclTracker.class) == null) {
                logger.error("No tracker found for {}, might have been explicitly disabled", coreName);
                continue;
            }


            // technically these metrics are not per core, but in order to filter in grafana the core is added as a tag
            Tags tags = Tags.of("core", coreName, "state", "Remaining");
            Gauge.builder("alfresco.transactions.nodes", coreAdminHandler,
                            x -> getTransactionsRemaining(x, coreName))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName);
            Gauge.builder("alfresco.transactions.nodes.lag", coreAdminHandler, x -> getTxLag(x, coreName))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName);
            Gauge.builder("alfresco.transactions.nodes.lastIndexCommitTime", coreAdminHandler,
                            x -> getLastIndexTxCommitTime(x, coreName))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, "state", "Remaining");
            Gauge.builder("alfresco.transactions.acls", coreAdminHandler, x -> getChangeSetsRemaining(x, coreName))
                    .tags(tags).register(registry);

            tags = Tags.of("core", coreName);
            Gauge.builder("alfresco.transactions.acls.lag", coreAdminHandler, x -> getChangeSetsLag(x, coreName))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName);
            Gauge.builder("alfresco.transactions.acls.lastIndexCommitTime", coreAdminHandler,
                            x -> getLastIndexChangeSetCommitTime(x, coreName))
                    .tags(tags)
                    .register(registry);
        }
    }


    private static TrackerState getAclsTrackerState(AlfrescoCoreAdminHandler coreAdminHandler, String coreName) {
        return coreAdminHandler.getTrackerRegistry()
                .getTrackerForCore(coreName, AclTracker.class)
                .getTrackerState();
    }

    private static TrackerState getMetadataTrackerState(AlfrescoCoreAdminHandler coreAdminHandler, String coreName) {
        return coreAdminHandler.getTrackerRegistry()
                .getTrackerForCore(coreName, MetadataTracker.class)
                .getTrackerState();
    }

    private static long getLastIndexChangeSetCommitTime(AlfrescoCoreAdminHandler coreAdminHandler, String coreName) {
        return getAclsTrackerState(coreAdminHandler, coreName).getLastIndexedChangeSetCommitTime();
    }

    private static long getChangeSetsLag(AlfrescoCoreAdminHandler coreAdminHandler, String coreName) {
        TrackerState aclsTrackerState = getAclsTrackerState(coreAdminHandler, coreName);
        long lastIndexChangeSetCommitTime = aclsTrackerState.getLastIndexedChangeSetCommitTime();
        long lastChangeSetCommitTimeOnServer = aclsTrackerState.getLastChangeSetCommitTimeOnServer();
        long changeSetLagSeconds = (lastChangeSetCommitTimeOnServer - lastIndexChangeSetCommitTime) / 1000;
        return ((changeSetLagSeconds < 0) ? 0 : changeSetLagSeconds);
    }

    private static long getChangeSetsRemaining(AlfrescoCoreAdminHandler coreAdminHandler, String coreName) {
        TrackerState aclsTrackerState = getAclsTrackerState(coreAdminHandler, coreName);
        long lastIndexedChangeSetId = aclsTrackerState.getLastIndexedChangeSetId();
        long lastChangeSetIdOnServer = aclsTrackerState.getLastChangeSetIdOnServer();
        long changeSetsToDo = lastChangeSetIdOnServer - lastIndexedChangeSetId;
        return ((changeSetsToDo < 0) ? 0 : changeSetsToDo);
    }

    private static long getLastIndexTxCommitTime(AlfrescoCoreAdminHandler coreAdminHandler, String coreName) {
        return getMetadataTrackerState(coreAdminHandler, coreName)
                .getLastIndexedTxCommitTime();
    }

    private static long getTxLag(AlfrescoCoreAdminHandler coreAdminHandler, String coreName) {
        TrackerState metadataTrackerState = getMetadataTrackerState(coreAdminHandler, coreName);
        long lastTxCommitTimeOnServer = metadataTrackerState.getLastTxCommitTimeOnServer();
        long lastIndexTxCommitTime = metadataTrackerState.getLastIndexedTxCommitTime();

        long txLagSeconds = (lastTxCommitTimeOnServer - lastIndexTxCommitTime) / 1000;
        return ((txLagSeconds < 0) ? 0 : txLagSeconds);
    }

    private static long getTransactionsRemaining(AlfrescoCoreAdminHandler coreAdminHandler, String coreName) {
        TrackerState metadataTrackerState = getMetadataTrackerState(coreAdminHandler, coreName);
        long lastIndexedTxId = metadataTrackerState.getLastIndexedTxId();
        long lastTxIdOnServer = metadataTrackerState.getLastTxIdOnServer();
        long transactionsToDo = lastTxIdOnServer - lastIndexedTxId;
        return (transactionsToDo < 0 ? 0 : transactionsToDo);
    }


}
