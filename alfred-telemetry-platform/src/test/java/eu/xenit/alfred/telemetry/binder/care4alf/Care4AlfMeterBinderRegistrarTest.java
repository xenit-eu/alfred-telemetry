package eu.xenit.alfred.telemetry.binder.care4alf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.telemetry.binder.MeterBinderRegistrar;
import eu.xenit.alfred.telemetry.config.CommonTagFilterFactory;
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

    private MeterRegistry globalRegistry;

    @Mock
    private ApplicationContext applicationContext;

    @BeforeEach
    void setup() {
        globalRegistry = new SimpleMeterRegistry();
        globalRegistry.config().meterFilter(CommonTagFilterFactory.commonTagsIfNotExists(Tags.of("application", "alfresco")));

        registrar = new Care4AlfMeterBinderRegistrar(globalRegistry);
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

        final Counter counterThroughGlobalRegistry = globalRegistry.counter("counter");
        assertThat(counterThroughGlobalRegistry.getId().getTag("application"), is("alfresco"));
    }

}