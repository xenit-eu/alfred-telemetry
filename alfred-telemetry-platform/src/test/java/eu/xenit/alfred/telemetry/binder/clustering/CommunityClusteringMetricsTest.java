package eu.xenit.alfred.telemetry.binder.clustering;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommunityClusteringMetricsTest {

    private CommunityClusteringMetrics communityClusteringMetrics;
    private MeterRegistry meterRegistry;

    @Mock
    private Properties globalProperties;

    @Test
    void testClusterMembersWith5Clusters() {
        meterRegistry = new SimpleMeterRegistry();
        communityClusteringMetrics = new CommunityClusteringMetrics(globalProperties);
        communityClusteringMetrics.bindTo(meterRegistry);

        Assertions.assertEquals(-1, meterRegistry.get("repository.nodes.count").gauge().value());
    }
}