package eu.xenit.alfred.telemetry.binder.dbcp;

public interface TelemetryBasicDataSource {

    double getNumActive();

    double getNumIdle();

    double getMaxActive();

    double getMinIdle();
}
