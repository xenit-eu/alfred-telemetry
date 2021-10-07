package eu.xenit.alfred.telemetry.solr.monitoring.registry;

import eu.xenit.alfred.telemetry.solr.util.Util;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.graphite.GraphiteMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RegistryRegistrar {
    private static RegistryRegistrar registrar = null;

    private static final Logger logger = LoggerFactory.getLogger(RegistryRegistrar.class);
    private static final CompositeMeterRegistry globalMeterRegistry = Metrics.globalRegistry;
    private GraphiteMeterRegistry graphiteMeterRegistry;
    private PrometheusMeterRegistry prometheusMeterRegistry;

    public static MeterRegistry getGlobalMeterRegistry() {
        return RegistryRegistrar.globalMeterRegistry;
    }

    public static PrometheusMeterRegistry getPrometheusMeterRegistry() {
        return getInstance().prometheusMeterRegistry;
    }

    public static GraphiteMeterRegistry getGraphiteMeterRegistry() {
        return getInstance().graphiteMeterRegistry;
    }

    public static RegistryRegistrar getInstance() {
        if(registrar == null) {
            registrar = new RegistryRegistrar();
        }
        return registrar;
    }

    private RegistryRegistrar() {
        Tags defaultTags = Tags.of("application", "solr", "host", tryToRetrieveHostName());
        String[] configuredTags = getConfiguredCommonTags();
        Tags commonTags = defaultTags.and(configuredTags);
        if(logger.isDebugEnabled()) {
            Arrays.stream(configuredTags).forEach(t -> logger.debug("-- Configured Tag: {}", t));
            commonTags.forEach(t -> logger.debug("-- Common Tag: {} = {}", t.getKey(), t.getValue()));
        }

        // always register the Prometheus registry
        registerPrometheus(commonTags);
        registerGraphite(commonTags);
    }

    private void registerGraphite(Tags tags) {
        if(!Util.isEnabled("ALFRED_TELEMETRY_EXPORT_GRAPHITE_ENABLED")) {
            return;
        }

        List<String> tagsKVList = new ArrayList<>();
        for(Tag t : tags) {
            tagsKVList.add(t.getKey());
            tagsKVList.add(t.getValue());
        }

        GraphiteConfig graphiteConfig = new GraphiteConfig(
                getEnvVarOrDefault("ALFRED_TELEMETRY_EXPORT_GRAPHITE_HOST","localhost"),
                Integer.parseInt(getEnvVarOrDefault("ALFRED_TELEMETRY_EXPORT_GRAPHITE_PORT",2004)),
                Integer.parseInt(getEnvVarOrDefault("ALFRED_TELEMETRY_EXPORT_GRAPHITE_STEP",5)),
                tagsKVList.toArray(new String[0])
        );
        graphiteMeterRegistry = new GraphiteMeterRegistry(graphiteConfig, Clock.SYSTEM);
        graphiteMeterRegistry.config().commonTags(tags);
        globalMeterRegistry.add(graphiteMeterRegistry);
    }

    private <T> String getEnvVarOrDefault(String envVar, T defaultValue) {
        return Optional.ofNullable(System.getenv(envVar))
                .orElse(defaultValue.toString());
    }

    private void registerPrometheus(Tags tags) {
        prometheusMeterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        prometheusMeterRegistry.config().commonTags(tags);
        globalMeterRegistry.add(prometheusMeterRegistry);
    }

    private String[] getConfiguredCommonTags() {
        String envVar = System.getenv("ALFRED_TELEMETRY_COMMON_TAGS");
        logger.debug("Common tags env var detected: {} ", envVar);

        if(envVar == null || envVar.isEmpty()) {
            logger.debug("Common tags disabled");
            return new String[0];
        }

        if(!envVar.contains(",")) {
            return Arrays.stream(envVar.split(":"))
                    .toArray(String[]::new);
        }

        return Util.parseList(envVar)
                .stream().map(s -> s.split(":"))
                .flatMap(Stream::of)
                .toArray(String[]::new);
    }

    private String tryToRetrieveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Unable to retrieve name of local host", e);
            return "unknown-host";
        }
    }
}
