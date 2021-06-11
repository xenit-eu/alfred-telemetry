package eu.xenit.alfred.telemetry.registry.prometheus;

import eu.xenit.alfred.telemetry.registry.AbstractRegistryConfig;

public class PrometheusConfig extends AbstractRegistryConfig {

    private int maxRequests;
    private int suppressMaxRequestsFailuresDuringUptimeMinutes;

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public int getSuppressMaxRequestsFailuresDuringUptimeMinutes() {
        return suppressMaxRequestsFailuresDuringUptimeMinutes;
    }

    public void setSuppressMaxRequestsFailuresDuringUptimeMinutes(int suppressMaxRequestsFailuresDuringUptimeMinutes) {
        this.suppressMaxRequestsFailuresDuringUptimeMinutes = suppressMaxRequestsFailuresDuringUptimeMinutes;
    }
}
