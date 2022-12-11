package eu.xenit.alfred.telemetry.binder.dbcp;

public interface TelemetryBasicDataSource {

    double getNumActive();

    double getNumIdle();

    double getMaxActive();

    double getMinIdle();

    static double getUsage(TelemetryBasicDataSource dataSource) {
        double maxSize = dataSource.getMaxActive();
        double currentSize = dataSource.getNumActive();
        if (maxSize < 0) {
            return -1D;
        }
        if (currentSize == 0) {
            return 0D;
        }
        return currentSize/maxSize;
    }
}
