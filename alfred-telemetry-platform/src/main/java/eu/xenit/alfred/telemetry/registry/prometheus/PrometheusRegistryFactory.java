package eu.xenit.alfred.telemetry.registry.prometheus;

import eu.xenit.alfred.telemetry.registry.RegistryFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class PrometheusRegistryFactory implements RegistryFactory {

    @Override
    public MeterRegistry createRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }
}
