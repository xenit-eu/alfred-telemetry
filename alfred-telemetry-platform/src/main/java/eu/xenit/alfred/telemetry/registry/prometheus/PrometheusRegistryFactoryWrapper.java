package eu.xenit.alfred.telemetry.registry.prometheus;

import eu.xenit.alfred.telemetry.registry.RegistryFactory;
import eu.xenit.alfred.telemetry.registry.RegistryFactoryWrapper;

public class PrometheusRegistryFactoryWrapper implements RegistryFactoryWrapper {

    private PrometheusConfig config;

    public PrometheusRegistryFactoryWrapper(PrometheusConfig config) {
        this.config = config;
    }

    @Override
    public boolean isRegistryEnabled() {
        return config.isEnabled();
    }

    @Override
    public String getRegistryClass() {
        return "io.micrometer.prometheus.PrometheusMeterRegistry";
    }

    @Override
    public RegistryFactory getRegistryFactory() {
        return new PrometheusRegistryFactory();
    }
}
