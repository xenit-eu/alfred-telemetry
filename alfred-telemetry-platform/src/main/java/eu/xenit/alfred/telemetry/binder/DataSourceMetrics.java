package eu.xenit.alfred.telemetry.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.alfresco.repo.tenant.TenantBasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceMetrics implements NamedMeterBinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceMetrics.class);

    @Override
    public String getName() {
        return "jdbc";
    }

    private final TenantBasicDataSource dataSource;

    public DataSourceMetrics(DataSource dataSource) {
        if (dataSource instanceof TenantBasicDataSource) {
            this.dataSource = (TenantBasicDataSource) dataSource;
            return;
        }

        LOGGER.warn("Monitoring not supported for dataSource of type '{}', expected dataSource of type '{}'",
                dataSource.getClass().getCanonicalName(),
                TenantBasicDataSource.class.getCanonicalName());
        this.dataSource = null;
    }

    @Override
    public void bindTo(@Nonnull MeterRegistry meterRegistry) {
        if (dataSource == null) {
            return;
        }

        dataSourceGaugeBuilder("jdbc.connections.count", TenantBasicDataSource::getNumActive)
                .tags("status", "active")
                .description("The current number of active connections that have been allocated from this data source.")
                .register(meterRegistry);
        dataSourceGaugeBuilder("jdbc.connections.count", TenantBasicDataSource::getNumIdle)
                .tags("status", "idle")
                .description(
                        "The current number of idle connections that are waiting to be allocated from this data source.")
                .register(meterRegistry);

        dataSourceGaugeBuilder("jdbc.connections.max", TenantBasicDataSource::getMaxActive)
                .description("The maximum number of active connections that can be allocated at the same time.")
                .register(meterRegistry);

        dataSourceGaugeBuilder("jdbc.connections.min", TenantBasicDataSource::getMinIdle)
                .description("The minimum number of idle connections in the pool.")
                .register(meterRegistry);

        dataSourceGaugeBuilder("jdbc.connections.usage", DataSourceMetrics::getUsage)
                .register(meterRegistry);
    }

    private Gauge.Builder<TenantBasicDataSource> dataSourceGaugeBuilder(String metricName,
                                                                    ToDoubleFunction<TenantBasicDataSource> function) {
        return Gauge.builder(metricName, dataSource, function)
                .tags("name", "dbcp");
    }

    private static Float getUsage(TenantBasicDataSource dataSource) {
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