package eu.xenit.alfred.telemetry.binder.dbcp;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

public class Dbcp2BasicDataSource extends AbstractBasicDataSource {

    private final BasicDataSource dataSource;

    private Dbcp2BasicDataSource(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static AbstractBasicDataSource createIfSupported(DataSource dataSource) {
        if(dataSource.getClass().isAssignableFrom(BasicDataSource.class)) {
            return new Dbcp2BasicDataSource((BasicDataSource)dataSource);
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
        return this.dataSource.getMaxTotal();
    }

    @Override
    public double getMinIdle() {
        return this.dataSource.getMinIdle();
    }
}
