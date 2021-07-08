package eu.xenit.alfred.telemetry.binder.clustering;

import eu.xenit.alfred.telemetry.binder.NamedMeterBinder;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.lang.NonNull;
import java.util.Properties;

public class CommunityClusteringMetrics implements NamedMeterBinder {
    private Properties globalProperties;

    public CommunityClusteringMetrics(Properties globalProperties) {
        this.globalProperties = globalProperties;
    }

    private int getClusterMemberCount(final Properties globalProperties) {
        // No clusters available on community
        return -1;
    }

    @Override
    public String getName() {
        return "clustering";
    }

    @Override
    public void bindTo(@NonNull MeterRegistry registry) {
        Gauge.builder(ClusteringMetrics.GAUGE_NAME, globalProperties, this::getClusterMemberCount)
                .description(ClusteringMetrics.GAUGE_DESCRIPTION)
                .register(registry);    }
}
