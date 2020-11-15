package eu.xenit.alfred.telemetry.solr.monitoring.registry;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryRegistraar {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryRegistraar.class);
    CompositeMeterRegistry globalMeterRegistry = Metrics.globalRegistry;
    PrometheusMeterRegistry prometheusMeterRegistry;

    public CompositeMeterRegistry getGlobalMeterRegistry() {
        return globalMeterRegistry;
    }

    public PrometheusMeterRegistry getPrometheusMeterRegistry() {
        return prometheusMeterRegistry;
    }

    public RegistryRegistraar() {
        prometheusMeterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        prometheusMeterRegistry.config().commonTags(Tags.of("application","solr"));
        globalMeterRegistry.add(prometheusMeterRegistry);
    }

}
