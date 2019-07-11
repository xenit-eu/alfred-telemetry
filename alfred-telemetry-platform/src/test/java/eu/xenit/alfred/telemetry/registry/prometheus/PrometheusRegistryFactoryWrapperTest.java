package eu.xenit.alfred.telemetry.registry.prometheus;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrometheusRegistryFactoryWrapperTest {

    @Mock
    private PrometheusConfig config;

    private PrometheusRegistryFactoryWrapper wrapper;

    @BeforeEach
    void setup() {
        wrapper = new PrometheusRegistryFactoryWrapper(config);
    }

    @Test
    void getRegistryClass() {
        assertThat(wrapper.getRegistryClass(), is(PrometheusMeterRegistry.class.getCanonicalName()));
    }

    @Test
    void enabled() {
        when(config.isEnabled()).thenReturn(true);

        assertThat(wrapper.isRegistryEnabled(), is(true));
    }

    @Test
    void disabled() {
        when(config.isEnabled()).thenReturn(false);

        assertThat(wrapper.isRegistryEnabled(), is(false));
    }

    @Test
    void getRegistryFactory() {
        assertThat(wrapper.getRegistryFactory(), is(instanceOf(PrometheusRegistryFactory.class)));
    }

}