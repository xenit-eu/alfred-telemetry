package eu.xenit.alfred.telemetry.registry.graphite;

import eu.xenit.alfred.telemetry.registry.RegistryFactory;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.graphite.GraphiteMeterRegistry;
import io.micrometer.graphite.GraphiteNamingConvention;
import java.time.Duration;
import javax.annotation.Nonnull;

public class GraphiteRegistryFactory implements RegistryFactory {

    private GraphiteConfig config;

    GraphiteRegistryFactory(GraphiteConfig config) {
        this.config = config;
    }

    @Override
    public MeterRegistry createRegistry() {
        io.micrometer.graphite.GraphiteConfig micrometerGraphiteConfig = new io.micrometer.graphite.GraphiteConfig() {
            @Override
            @Nonnull
            public String host() {
                return config.getHost();
            }

            @Override
            public int port() {
                return config.getPort();
            }

            @Override
            @Nonnull
            public Duration step() {
                return Duration.ofSeconds(config.getStep());
            }

            @Override
            public String get(@Nonnull String key) {
                return null;
            }

            @Override
            public String[] tagsAsPrefix() {
                return config.getTagsAsPrefix().toArray(new String[]{});
            }
        };

        GraphiteMeterRegistry graphiteRegistry = new GraphiteMeterRegistry(
                micrometerGraphiteConfig,
                Clock.SYSTEM);
        graphiteRegistry.config().namingConvention(GraphiteNamingConvention.dot);

        return graphiteRegistry;
    }
}
