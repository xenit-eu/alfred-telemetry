package eu.xenit.alfred.telemetry.binder;

import eu.xenit.alfred.telemetry.binder.dbcp.AbstractBasicDataSource;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

public class DataSourceMetrics implements NamedMeterBinder {

    @Override
    public String getName() {
        return "jdbc";
    }

    private final AbstractBasicDataSource dataSource;

    public DataSourceMetrics(DataSource dataSource) {
        this.dataSource = AbstractBasicDataSource.build(dataSource);
    }

    @Override
    public void bindTo(@Nonnull MeterRegistry meterRegistry) {
        if (dataSource == null) {
            return;
        }

        dataSourceGaugeBuilder("jdbc.connections.count", AbstractBasicDataSource::getNumActive)
                .tags("status", "active")
                .description("The current number of active connections that have been allocated from this data source.")
                .register(meterRegistry);
        dataSourceGaugeBuilder("jdbc.connections.count", AbstractBasicDataSource::getNumIdle)
                .tags("status", "idle")
                .description(
                        "The current number of idle connections that are waiting to be allocated from this data source.")
                .register(meterRegistry);

        dataSourceGaugeBuilder("jdbc.connections.max", AbstractBasicDataSource::getMaxActive)
                .description("The maximum number of active connections that can be allocated at the same time.")
                .register(meterRegistry);

        dataSourceGaugeBuilder("jdbc.connections.min", AbstractBasicDataSource::getMinIdle)
                .description("The minimum number of idle connections in the pool.")
                .register(meterRegistry);

        dataSourceGaugeBuilder("jdbc.connections.usage", AbstractBasicDataSource::getUsage)
                .register(meterRegistry);
    }

    private Gauge.Builder<AbstractBasicDataSource> dataSourceGaugeBuilder(String metricName,
                                                                  ToDoubleFunction<AbstractBasicDataSource> function) {
        return Gauge.builder(metricName, dataSource, function)
                .tags("name", "dbcp");
    }
}