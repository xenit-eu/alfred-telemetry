package eu.xenit.alfred.telemetry.binder.dbcp;

import eu.xenit.alfred.telemetry.annotation.GeneratedModel;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

@GeneratedModel
public class Dbcp2BasicDataSource implements TelemetryBasicDataSource {

    private final BasicDataSource dataSource;

    public Dbcp2BasicDataSource(DataSource dataSource) {
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
        return this.dataSource.getMaxTotal();
    }

    @Override
    public double getMinIdle() {
        return this.dataSource.getMinIdle();
    }
}
