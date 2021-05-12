package eu.xenit.alfred.telemetry.registry.prometheus;

import eu.xenit.alfred.telemetry.registry.AbstractRegistryConfig;

public class PrometheusConfig extends AbstractRegistryConfig {

    private int maxRequests;

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }
}
