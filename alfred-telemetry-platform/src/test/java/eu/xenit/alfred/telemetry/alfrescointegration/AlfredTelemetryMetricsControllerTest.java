package eu.xenit.alfred.telemetry.alfrescointegration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import org.alfresco.enterprise.metrics.MetricsController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AlfredTelemetryMetricsControllerTest {

    @Mock
    private CompositeMeterRegistry meterRegistry;
    @Mock
    private MetricsController alfrescoMetricsController;
    @Mock
    private MeterRegistry alfrescoDefaultRegistry;

    @BeforeEach
    void setup() {
        when(alfrescoMetricsController.getRegistry()).thenReturn(alfrescoDefaultRegistry);
    }

    @Test
    void initialize_includeAlfrescoDefaultRegistry() {
        new AlfredTelemetryMetricsController(meterRegistry, alfrescoMetricsController, true);

        verify(alfrescoMetricsController).getRegistry();
        verify(meterRegistry).add(alfrescoDefaultRegistry);
    }

    @Test
    void initialize_excludeAlfrescoDefaultRegistry() {
        new AlfredTelemetryMetricsController(meterRegistry, alfrescoMetricsController, false);

        verify(alfrescoMetricsController, never()).getRegistry();
        verify(meterRegistry, never()).add(alfrescoDefaultRegistry);
    }

    @Test
    void getRegistry_returnsGlobalRegistry() {
        MetricsController controller = new AlfredTelemetryMetricsController(meterRegistry, alfrescoMetricsController,
                true);
        assertThat(controller.getRegistry(), instanceOf(CompositeMeterRegistry.class));
    }

    @Test
    void isEnabled_delegatesToAlfrescoDefaultRegistry() {
        when(alfrescoMetricsController.isEnabled()).thenReturn(true);

        MetricsController controller = new AlfredTelemetryMetricsController(meterRegistry, alfrescoMetricsController,
                true);
        assertThat(controller.isEnabled(), is(true));
        verify(alfrescoMetricsController).isEnabled();
    }

    @Test
    void getScrapeData_delegatesToAlfrescoDefaultRegistry() {
        final String mockScrapeData = "ScRaPeDaTa123";
        when(alfrescoMetricsController.getScrapeData()).thenReturn(mockScrapeData);

        MetricsController controller = new AlfredTelemetryMetricsController(meterRegistry, alfrescoMetricsController,
                false);
        assertThat(controller.getScrapeData(), is(mockScrapeData));
        verify(alfrescoMetricsController).getScrapeData();
    }

}