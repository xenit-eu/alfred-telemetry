package eu.xenit.alfred.telemetry.binder.care4alf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.telemetry.binder.MeterBinderRegistrar;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class Care4AlfMeterBinderRegistrarTest {

    private MeterBinderRegistrar registrar;

    @Mock
    private ApplicationContext applicationContext;

    @BeforeEach
    void setup() {
        when(applicationContext.getBeansOfType(MeterFilter.class))
                .thenReturn(
                        Collections.singletonMap("filter", MeterFilter.commonTags(Tags.of("application", "alfresco"))));
        MeterRegistry registry = new SimpleMeterRegistry();
        registrar = new Care4AlfMeterBinderRegistrar(registry);
        registrar.setApplicationContext(applicationContext);
        registrar.setEnabled(true);
    }

    /**
     * Metrics exposed through the Care4Alf registrar, should contain the Care4Alf specific common tags.
     */
    @Test
    void commonTags() {
        registrar.afterPropertiesSet();
        final Counter counter = registrar.getMeterRegistry().counter("counter");
        assertThat(counter.getId().getTag("application"), is("c4a"));
    }

}