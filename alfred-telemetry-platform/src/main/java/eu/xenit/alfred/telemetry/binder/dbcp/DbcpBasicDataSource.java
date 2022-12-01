package eu.xenit.alfred.telemetry.binder.dbcp;

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;

public class DbcpBasicDataSource extends AbstractBasicDataSource {
    private final BasicDataSource dataSource;

    private DbcpBasicDataSource(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static AbstractBasicDataSource createIfSupported(DataSource dataSource) {
        if(dataSource.getClass().isAssignableFrom(BasicDataSource.class)) {
            return new DbcpBasicDataSource((BasicDataSource)dataSource);
        }
        return null;
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
