package eu.xenit.alfred.telemetry.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class for the {@link PrometheusMeterRegistry} to encapsulate potentially unavailable imports.
 */
public class PrometheusRegistryUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusRegistryUtil.class);

    private PrometheusRegistryUtil() {
        // private ctor to hide implicit public one
    }

    public static boolean isOrContainsPrometheusRegistry(final MeterRegistry meterRegistry) {
        return tryToExtractPrometheusRegistry(meterRegistry) != null;
    }

    public static String extractPrometheusScrapeData(final MeterRegistry meterRegistry) {
        PrometheusMeterRegistry castedRegistry = tryToExtractPrometheusRegistry(meterRegistry);

        if (castedRegistry == null) {
            throw new IllegalArgumentException("Cannot extract prometheus scrape date out of meterRegistry");
        }

        return castedRegistry.scrape();
    }

    private static PrometheusMeterRegistry tryToExtractPrometheusRegistry(final MeterRegistry meterRegistry) {
        if (meterRegistry instanceof PrometheusMeterRegistry) {
            return (PrometheusMeterRegistry) meterRegistry;
        }

        if (meterRegistry instanceof CompositeMeterRegistry) {

            PrometheusMeterRegistry prometheusMeterRegistry = null;

            for (final MeterRegistry childRegistry : ((CompositeMeterRegistry) meterRegistry).getRegistries()) {
                if (childRegistry instanceof PrometheusMeterRegistry) {
                    prometheusMeterRegistry = (PrometheusMeterRegistry) childRegistry;
                }

                if (childRegistry instanceof CompositeMeterRegistry) {
                    prometheusMeterRegistry = tryToExtractPrometheusRegistry(childRegistry);
                }

                if (prometheusMeterRegistry != null) {
                    break;
                }
            }

            return prometheusMeterRegistry;
        }

        LOGGER.debug("MeterRegistry of type '{}' cannot be converted to a PrometheusMeterRegistry",
                meterRegistry.getClass().getCanonicalName());
        return null;
    }
}
