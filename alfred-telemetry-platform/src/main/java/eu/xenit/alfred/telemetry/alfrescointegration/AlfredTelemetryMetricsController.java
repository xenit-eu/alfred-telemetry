package eu.xenit.alfred.telemetry.alfrescointegration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import org.alfresco.enterprise.metrics.MetricsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Alfresco's {@link MetricsController} that will offer the global MicroMeter registry as registry.
 */
public class AlfredTelemetryMetricsController implements MetricsController {

    private static final Logger log = LoggerFactory.getLogger(AlfredTelemetryMetricsController.class);

    private static final String MSG_INTEGRATION_FAILED =
            "Failed to integrate Alfred Telemetry with the out of the box Alfresco metrics: ";

    private final CompositeMeterRegistry globalRegistry;
    private final MetricsController alfrescoMetricsController;

    AlfredTelemetryMetricsController(CompositeMeterRegistry globalRegistry,
            MetricsController alfrescoMetricsController,
            boolean enableDefaultAlfrescoRegistry) {
        this.globalRegistry = globalRegistry;
        this.alfrescoMetricsController = alfrescoMetricsController;

        this.registerAlfrescoControllerInGlobalRegistry(enableDefaultAlfrescoRegistry);
    }

    private void registerAlfrescoControllerInGlobalRegistry(final boolean enableDefaultAlfrescoRegistry) {
        if (enableDefaultAlfrescoRegistry) {
            if (!alfrescoMetricsController.isEnabled()) {
                log.warn(MSG_INTEGRATION_FAILED
                        + "out of the box Alfresco metrics not enabled, see ${metrics.enabled} property.");
                return;
            }
            final MeterRegistry alfrescoRegistry = alfrescoMetricsController.getRegistry();
            if (alfrescoRegistry == null) {
                log.warn(MSG_INTEGRATION_FAILED + "out of the box Alfresco MeterRegistry not correctly initialized.");
                return;
            }
            globalRegistry.add(alfrescoRegistry);
        }
    }

    @Override
    public MeterRegistry getRegistry() {
        return globalRegistry;
    }

    @Override
    public boolean isEnabled() {
        return alfrescoMetricsController.isEnabled();
    }

    @Override
    public String getScrapeData() {
        // This scraping is prometheus specific, falling back to Alfresco's implementation
        return alfrescoMetricsController.getScrapeData();
    }
}
