package eu.xenit.alfred.telemetry.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nonnull;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceMetrics implements NamedMeterBinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceMetrics.class);

    @Override
    public String getName() {
        return "jdbc";
    }

    private final BasicDataSource dataSource;

    public DataSourceMetrics(DataSource dataSource) {
        if (dataSource instanceof BasicDataSource) {
            this.dataSource = (BasicDataSource) dataSource;
        } else {
            LOGGER.warn("Monitoring not supported for dataSource of type '{}', expected dataSource of type '{}'",
                    dataSource.getClass().getCanonicalName(),
                    BasicDataSource.class.getCanonicalName());
            this.dataSource = null;
        }
    }

    @Override
    public void bindTo(@Nonnull MeterRegistry meterRegistry) {
        if (dataSource == null) {
            return;
        }

        dataSourceGaugeBuilder("jdbc.connections.count", BasicDataSource::getNumActive)
                .tags("status", "active")
                .description("The current number of active connections that have been allocated from this data source.")
                .register(meterRegistry);
        dataSourceGaugeBuilder("jdbc.connections.count", BasicDataSource::getNumIdle)
                .tags("status", "idle")
                .description(
                        "The current number of idle connections that are waiting to be allocated from this data source.")
                .register(meterRegistry);

        dataSourceGaugeBuilder("jdbc.connections.max", BasicDataSource::getMaxActive)
                .description("The maximum number of active connections that can be allocated at the same time.")
                .register(meterRegistry);

        dataSourceGaugeBuilder("jdbc.connections.min", BasicDataSource::getMinIdle)
                .description("The minimum number of idle connections in the pool.")
                .register(meterRegistry);

        dataSourceGaugeBuilder("jdbc.connections.usage", this::getUsage)
                .register(meterRegistry);
    }

    private Gauge.Builder dataSourceGaugeBuilder(String metricName, ToDoubleFunction<BasicDataSource> function) {
        return Gauge.builder(metricName, dataSource, function)
                .tags("name", "dbcp");
    }

    private Float getUsage(BasicDataSource dataSource) {
        int maxSize = dataSource.getMaxActive();
        int currentSize = dataSource.getNumActive();
        if (maxSize < 0) {
            return -1F;
        }
        if (currentSize == 0) {
            return 0F;
        }
        return (float) currentSize / (float) maxSize;
    }
}