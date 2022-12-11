package eu.xenit.alfred.telemetry.binder.clustering;

import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.RequiredSearch;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.HashSet;
import org.alfresco.enterprise.repo.cluster.core.ClusterService;
import org.alfresco.enterprise.repo.cluster.core.RegisteredServerInfoImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClusteringMetricsTest {

    @Mock
    private ClusterService clusterService;

    private ClusteringMetrics clusteringMetrics;

    private MeterRegistry meterRegistry;

    void initClusterMetricsWithClusterService() {
        meterRegistry = new SimpleMeterRegistry();
        clusteringMetrics = new ClusteringMetrics(clusterService);
        MockitoAnnotations.initMocks(this);
    }

    void initClusterMetricsWithoutClusterService() {
        meterRegistry = new SimpleMeterRegistry();
        clusteringMetrics = new ClusteringMetrics(null);
    }

    @Test
    void testClusterMembersWith5Clusters() {
        initClusterMetricsWithClusterService();
        when(clusterService.isClusteringEnabled()).thenReturn(true);
        when(clusterService.getNumActiveClusterMembers()).thenReturn(5);
        Assertions.assertEquals(5, clusteringMetrics.getClusterMemberCount(clusterService));
    }

    @Test
    void testClusterMembersWhenCLusterServiceIsNull() {
        initClusterMetricsWithoutClusterService();
        Assertions.assertEquals(0, clusteringMetrics.getClusterMemberCount(clusterService));
    }

    @Test
    void testNonClusterMembersWith1Clusters() {
        initClusterMetricsWithClusterService();
        final String ipAddress = "127.0.0.0";
        final Integer port = 8000;
        HashSet<RegisteredServerInfoImpl> registeredServerInfos = new HashSet<>();
        registeredServerInfos.add(new RegisteredServerInfoImpl());
        when(clusterService.getMemberIP()).thenReturn(ipAddress);
        when(clusterService.getMemberPort()).thenReturn(port);
        when(clusterService.getRegisteredNonMembers(ipAddress, port)).thenReturn(registeredServerInfos);
        Assertions.assertEquals(1, clusteringMetrics.getNonMemberCount(clusterService));
    }

    @Test
    void testNonClusterMetricsWhenCLusterServiceIsNull() {
        initClusterMetricsWithoutClusterService();
        Assertions.assertEquals(0, clusteringMetrics.getClusterMemberCount(null));
        Assertions.assertEquals(0, clusteringMetrics.getNonMemberCount(null));
        Assertions.assertEquals(0, clusteringMetrics.getOfflineMemberCount(null));
    }

    @Test
    void testOfflineClusterMembersWith1Clusters() {
        initClusterMetricsWithClusterService();
        HashSet<RegisteredServerInfoImpl> registeredServerInfos = new HashSet<>();
        registeredServerInfos.add(new RegisteredServerInfoImpl());
        when(clusterService.isClusteringEnabled()).thenReturn(true);
        when(clusterService.getOfflineMembers()).thenReturn(registeredServerInfos);
        Assertions.assertEquals(1, clusteringMetrics.getOfflineMemberCount(clusterService));
    }

    @Test
    void testOfflineClusterMembersWhenCLusterServiceIsNull() {
        initClusterMetricsWithoutClusterService();
        Assertions.assertEquals(0, clusteringMetrics.getOfflineMemberCount(clusterService));
    }

    @Test
    void testClusterCountsWhenClusteringIsDisabled() {
        initClusterMetricsWithClusterService();
        when(clusterService.isClusteringEnabled()).thenReturn(false);
        Assertions.assertEquals(0, clusteringMetrics.getOfflineMemberCount(clusterService));
        Assertions.assertEquals(0, clusteringMetrics.getClusterMemberCount(clusterService));
    }

    @Test
    void testBindTo() {
        initClusterMetricsWithClusterService();
        clusteringMetrics.bindTo(meterRegistry);
        RequiredSearch search = meterRegistry.get(ClusteringMetrics.GAUGE_NAME);
        Assertions.assertEquals(3, search.gauges().size());
    }
}