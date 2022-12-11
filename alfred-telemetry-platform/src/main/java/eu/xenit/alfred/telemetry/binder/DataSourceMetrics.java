package eu.xenit.alfred.telemetry.binder;

import eu.xenit.alfred.telemetry.binder.dbcp.TelemetryBasicDataSource;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.ToDoubleFunction;
import javax.annotation.Nonnull;

public class DataSourceMetrics implements NamedMeterBinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceMetrics.class);

    @Override
    public String getName() {
        return "jdbc";
    }

    private final TelemetryBasicDataSource basicDataSource;

    public DataSourceMetrics(TelemetryBasicDataSource versionSpecificBasicDataSource) {
        this.basicDataSource = versionSpecificBasicDataSource;
    }

    @Override
    public void bindTo(@Nonnull MeterRegistry meterRegistry) {
        if (basicDataSource == null) {
            LOGGER.debug("basicDataSource is null, aborting!");
            return;
        }

        dataSourceGaugeBuilder("jdbc.connections.count", TelemetryBasicDataSource::getNumActive)
                .tags("status", "active")
                .description("The current number of active connections that have been allocated from this data source.")
                .register(meterRegistry);
        dataSourceGaugeBuilder("jdbc.connections.count", TelemetryBasicDataSource::getNumIdle)
                .tags("status", "idle")
                .description(
                        "The current number of idle connections that are waiting to be allocated from this data source.")
                .register(meterRegistry);

        dataSourceGaugeBuilder("jdbc.connections.max", TelemetryBasicDataSource::getMaxActive)
                .description("The maximum number of active connections that can be allocated at the same time.")
                .register(meterRegistry);

        dataSourceGaugeBuilder("jdbc.connections.min", TelemetryBasicDataSource::getMinIdle)
                .description("The minimum number of idle connections in the pool.")
                .register(meterRegistry);

        dataSourceGaugeBuilder("jdbc.connections.usage", this::getUsage)
                .register(meterRegistry);
    }

    private Gauge.Builder<TelemetryBasicDataSource> dataSourceGaugeBuilder(String metricName,
                                                                           ToDoubleFunction<TelemetryBasicDataSource> function) {
        return Gauge.builder(metricName, basicDataSource, function)
                .tags("name", "dbcp");
    }

    private double getUsage(TelemetryBasicDataSource dataSource) {
        double maxSize = dataSource.getMaxActive();
        double currentSize = dataSource.getNumActive();
        if (maxSize < 0) {
            return -1F;
        }
        if (currentSize == 0) {
            return 0F;
        }
        return (float) currentSize / (float) maxSize;
    }
}