package eu.xenit.alfred.telemetry.binder.dbcp;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public abstract class AbstractBasicDataSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBasicDataSource.class);

    public static AbstractBasicDataSource build(DataSource dataSource) {
        LOGGER.debug("Instantiating Data Source Metrics");
        AbstractBasicDataSource instance = DbcpBasicDataSource.createIfSupported(dataSource);
        if(instance != null) {
            return instance;
        }

        instance = Dbcp2BasicDataSource.createIfSupported(dataSource);
        if(instance != null) {
            return instance;
        }

        LOGGER.warn("Monitoring not supported for dataSource of type '{}', expected dataSource of type '{}'",
                dataSource.getClass().getCanonicalName(),
                BasicDataSource.class.getCanonicalName());
        return null;
    }

    public abstract double getNumActive();

    public abstract double getNumIdle();

    public abstract double getMaxActive();

    public abstract double getMinIdle();

    public static double getUsage(AbstractBasicDataSource basicDataSource) {
        double maxSize = basicDataSource.getMaxActive();
        double currentSize = basicDataSource.getNumActive();
        if (maxSize < 0) {
            return -1F;
        }
        if (currentSize == 0) {
            return 0F;
        }
        return (float) currentSize / (float) maxSize;
    }
}
