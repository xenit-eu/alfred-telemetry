package eu.xenit.alfred.telemetry.alfrescointegration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import org.alfresco.enterprise.metrics.MetricsController;

/**
 * Implementation of Alfresco's {@link MetricsController} that will offer the global MicroMeter registry as registry.
 */
public class AlfredTelemetryMetricsController implements MetricsController {

    private CompositeMeterRegistry globalRegistry;
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
            globalRegistry.add(alfrescoMetricsController.getRegistry());
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
