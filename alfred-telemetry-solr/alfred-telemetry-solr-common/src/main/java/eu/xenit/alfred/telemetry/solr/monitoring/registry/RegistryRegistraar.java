package eu.xenit.alfred.telemetry.solr.monitoring.registry;

import eu.xenit.alfred.telemetry.solr.util.Util;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.graphite.GraphiteMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryRegistraar {
    private static RegistryRegistraar registraar = null;

    private static final Logger logger = LoggerFactory.getLogger(RegistryRegistraar.class);
    CompositeMeterRegistry globalMeterRegistry = Metrics.globalRegistry;
    PrometheusMeterRegistry prometheusMeterRegistry;
    GraphiteMeterRegistry graphiteMeterRegistry;


    public static RegistryRegistraar getInstance() {
        if(registraar == null)
            registraar = new RegistryRegistraar();
        return registraar;
    }

    private RegistryRegistraar() {
        // always register the Prometheus registry
        prometheusMeterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        prometheusMeterRegistry.config().commonTags(Tags.of("application", "solr"));
        globalMeterRegistry.add(prometheusMeterRegistry);


        if(Util.isEnabled("ALFRED_TELEMETRY_EXPORT_GRAPHITE_ENABLED")) {
            MyGraphiteConfig graphiteConfig = new MyGraphiteConfig();
            graphiteConfig.setHost(System.getenv("ALFRED_TELEMETRY_EXPORT_GRAPHITE_HOST")!=null?System.getenv("ALFRED_TELEMETRY_EXPORT_GRAPHITE_HOST"):"localhost");
            graphiteConfig.setPort(System.getenv("ALFRED_TELEMETRY_EXPORT_GRAPHITE_PORT")!=null?Integer.parseInt(System.getenv("ALFRED_TELEMETRY_EXPORT_GRAPHITE_PORT")):2004);
            graphiteConfig.setStep(System.getenv("ALFRED_TELEMETRY_EXPORT_GRAPHITE_STEP")!=null?Integer.parseInt(System.getenv("ALFRED_TELEMETRY_EXPORT_GRAPHITE_STEP")):5);
            graphiteConfig.setTagsAsPrefix("application,host");
            io.micrometer.graphite.GraphiteConfig micrometerGraphiteConfig = new io.micrometer.graphite.GraphiteConfig() {
                @Override
                public String host() {
                    return graphiteConfig.getHost();
                }

                @Override
                public int port() {
                    return graphiteConfig.getPort();
                }

                @Override
                public Duration step() {
                    return Duration.ofSeconds(graphiteConfig.getStep());
                }

                @Override
                public String get(String key) {
                    return null;
                }

                @Override
                public String[] tagsAsPrefix() {
                    return graphiteConfig.getTagsAsPrefix().toArray(new String[]{});
                }
            };
            graphiteMeterRegistry = new GraphiteMeterRegistry(micrometerGraphiteConfig,
                    io.micrometer.core.instrument.Clock.SYSTEM);
            graphiteMeterRegistry.config().commonTags(Tags.of("application", "solr", "host", tryToRetrieveHostName()));
            globalMeterRegistry.add(graphiteMeterRegistry);
        }
    }

    private String tryToRetrieveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Unable to retrieve name of local host", e);
            return "unknown-host";
        }
    }

    public static MeterRegistry getGlobalMeterRegistry() {
        return getInstance().globalMeterRegistry;
    }

    public static PrometheusMeterRegistry getPrometheusMeterRegistry() {
        return getInstance().prometheusMeterRegistry;
    }

}
