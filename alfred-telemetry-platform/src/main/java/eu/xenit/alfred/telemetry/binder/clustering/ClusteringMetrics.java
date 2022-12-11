package eu.xenit.alfred.telemetry.binder.clustering;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import javax.annotation.Nonnull;
import org.alfresco.enterprise.repo.cluster.core.ClusterService;

import java.util.concurrent.atomic.AtomicBoolean;

public class ClusteringMetrics implements MeterBinder {
    private static final String CLUSTER_TYPE = "clustertype";
    public static final String GAUGE_NAME = "repository.cluster.nodes.count";
    public static final String GAUGE_DESCRIPTION = "The amount of repository cluster nodes";

    private ClusterService clusterService;
    private final AtomicBoolean clusterInitializationTriggered = new AtomicBoolean(false);

    public ClusteringMetrics(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {
        ensureClusterServiceInitialized();
        Gauge.builder(GAUGE_NAME, clusterService, this::getClusterMemberCount)
                .description(GAUGE_DESCRIPTION)
                .tags(CLUSTER_TYPE, "member")
                .register(registry);
        Gauge.builder(GAUGE_NAME, clusterService, this::getNonMemberCount)
                .description(GAUGE_DESCRIPTION)
                .tags(CLUSTER_TYPE, "non-member")
                .register(registry);
        Gauge.builder(GAUGE_NAME, clusterService, this::getOfflineMemberCount)
                .description(GAUGE_DESCRIPTION)
                .tags(CLUSTER_TYPE, "offline")
                .register(registry);
    }

    protected int getClusterMemberCount(final ClusterService clusterService) {
        if (clusterService == null) {
            return 0;
        }
        ensureClusterServiceInitialized();
        return clusterService.isClusteringEnabled() ? clusterService.getNumActiveClusterMembers() : 0;
    }

    protected int getNonMemberCount(final ClusterService clusterService) {
        if (clusterService == null) {
            return 0;
        }
        ensureClusterServiceInitialized();
        final String ipAddress = clusterService.getMemberIP();
        final Integer port = clusterService.getMemberPort();
        return clusterService.getRegisteredNonMembers(ipAddress, port).size();
    }

    protected int getOfflineMemberCount(final ClusterService clusterService) {
        if (clusterService == null) {
            return 0;
        }
        ensureClusterServiceInitialized();
        return clusterService.isClusteringEnabled() ? clusterService.getOfflineMembers().size() : 0;
    }

    private void ensureClusterServiceInitialized() {
        if (clusterService != null && !clusterService.isInitialised() && !clusterInitializationTriggered.get()) {
            clusterService.initClusterService();
            clusterInitializationTriggered.set(true);
        }
    }
}
