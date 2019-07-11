package eu.xenit.alfred.telemetry.binder.care4alf;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.sql.DataSource;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyJdbcMetrics implements MeterBinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyJdbcMetrics.class);

    private BasicDataSource dataSource;
    private DescriptorDAO currentRepoDescriptorDAO;
    private Properties globalProps;

    public LegacyJdbcMetrics(DataSource dataSource, DescriptorDAO currentRepoDescriptorDAO, Properties globalProps) {
        if (dataSource instanceof BasicDataSource) {
            this.dataSource = (BasicDataSource) dataSource;
        } else {
            LOGGER.warn("Monitoring not supported for dataSource of type '{}', expected dataSource of type '{}'",
                    dataSource.getClass().getCanonicalName(),
                    BasicDataSource.class.getCanonicalName());
            this.dataSource = null;
        }
        this.currentRepoDescriptorDAO = currentRepoDescriptorDAO;
        this.globalProps = globalProps;
    }

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {
        if (dataSource == null) {
            return;
        }

        Gauge.builder("db.connectionpool.max", dataSource, BasicDataSource::getMaxActive)
                .description("The maximum number of active connections that can be allocated at the same time.")
                .register(registry);
        Gauge.builder("db.connectionpool.active", dataSource, BasicDataSource::getNumActive)
                .description("The current number of active connections that have been allocated from this data source.")
                .register(registry);
        Gauge.builder("db.connectionpool.initial", dataSource, BasicDataSource::getInitialSize)
                .description("The initial size of the connection pool.")
                .register(registry);

        Gauge.builder("db.connectionpool.idle.min", dataSource, BasicDataSource::getMinIdle)
                .description("The minimum number of idle connections in the pool.")
                .register(registry);
        Gauge.builder("db.connectionpool.idle.max", dataSource, BasicDataSource::getMaxIdle)
                .description("The maximum number of connections that can remain idle in the pool.")
                .register(registry);

        Gauge.builder("db.connectionpool.wait.max", dataSource, BasicDataSource::getMaxWait)
                .description("The maximum number of milliseconds that the pool will wait for a connection to be "
                        + "returned before throwing an exception.")
                .register(registry);
        Gauge.builder("db.connectionpool.wait.active", dataSource, BasicDataSource::getMaxActive)
                .description("The maximum number of active connections that can be allocated at the same time.")
                .register(registry);

        Gauge.builder("db.healthy", currentRepoDescriptorDAO, this::dbCheck)
                .description("Whether the database repository descriptor can be obtained.")
                .register(registry);
        Gauge.builder("db.ping", dataSource, this::getDBPing)
                .description("the latency to the database is in milliseconds.")
                .baseUnit("milliseconds")
                .register(registry);
    }

    private int dbCheck(DescriptorDAO descriptorDAO) {
        if (descriptorDAO == null) {
            return -1;
        }

        try {
            descriptorDAO.getDescriptor();
            return 1;
        } catch (Exception e) {
            LOGGER.debug("Unable to retrieve repository descriptor", e);
            return -1;
        }
    }

    private long getDBPing(BasicDataSource dataSource) {
        String dbip = globalProps.getProperty("eu.xenit.c4a.metrics.dbip");
        if (dbip == null) {
            URI uri = URI.create(dataSource.getUrl());
            return this.ping(uri.getHost());
        } else {
            return this.ping(dbip);
        }
    }

    private long ping(String host) {
        long start = System.currentTimeMillis();
        try {
            if (InetAddress.getByName(host).isReachable(2000)) {
                return System.currentTimeMillis() - start;
            }
        } catch (IOException e) {
            return -1;
        }
        return -1;
    }
}
