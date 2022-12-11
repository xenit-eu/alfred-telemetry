package eu.xenit.alfred.telemetry.binder.dbcp;

import org.apache.commons.dbcp.BasicDataSource;
import javax.sql.DataSource;

public class DbcpBasicDataSource implements TelemetryBasicDataSource {
    private final BasicDataSource dataSource;

    public DbcpBasicDataSource(DataSource dataSource) {
        this.dataSource = (BasicDataSource)dataSource;
    }

    @Override
    public double getNumActive() {
        return this.dataSource.getNumActive();
    }

    @Override
    public double getNumIdle() {
        return this.dataSource.getNumIdle();
    }

    @Override
    public double getMaxActive() {
        return this.dataSource.getMaxActive();
    }

    @Override
    public double getMinIdle() {
        return this.dataSource.getMinIdle();
    }
}
