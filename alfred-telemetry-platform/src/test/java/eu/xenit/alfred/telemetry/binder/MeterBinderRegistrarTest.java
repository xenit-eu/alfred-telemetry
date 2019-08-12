package eu.xenit.alfred.telemetry.binder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Collections;
import java.util.Properties;
import org.alfresco.enterprise.repo.cluster.core.ClusterService;
import org.alfresco.enterprise.repo.cluster.core.ClusterServiceInitialisedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
// to avoid UnnecessaryStubbingException:
@MockitoSettings(strictness = Strictness.LENIENT)
class MeterBinderRegistrarTest {

    private MeterBinderRegistrar registrar;

    @Mock
    private ApplicationContext applicationContext;
    private Properties properties;

    @BeforeEach
    void setup() {
        registrar = new MeterBinderRegistrar(new SimpleMeterRegistry());
        registrar.setApplicationContext(applicationContext);
        registrar.setEnabled(true);
        properties = new Properties();
        registrar.setProperties(properties);
    }

    @Test
    void getMeterNameFromClass() {
        assertThat(
                MeterBinderRegistrar.getMeterNameFromClass(MeterBinderRegistrar.class),
                is(equalTo("meter-binder-registrar"))
        );
    }

    @Nested
    class BasicTests {

        private BasicTestMetrics basicTestMetrics;

        @BeforeEach
        void additionalSetup() {
            basicTestMetrics = new BasicTestMetrics();
            when(applicationContext.getBeansOfType(MeterBinder.class))
                    .thenReturn(Collections.singletonMap("testEventTriggeredMetrics", basicTestMetrics));
        }

        @Test
        void defaultBehaviour() {
            registrar.afterPropertiesSet();

            assertThat("BasicTestMetrics#bindTo should be executed once with the default configuration",
                    basicTestMetrics.getBindToExecutions(),
                    is(1));
        }

        @Test
        void disabledGlobally() {
            registrar.setEnabled(false);
            registrar.afterPropertiesSet();

            assertThat("BasicTestMetrics#bindTo should not have been executed if the registrar is disabled",
                    basicTestMetrics.getBindToExecutions(),
                    is(0));
        }

        @Test
        void disableSpecificMeterBinder() {
            properties.put(MeterBinderRegistrar.PROP_BINDER_PREFIX + "basic-test"
                    + MeterBinderRegistrar.PROP_BINDER_SUFFIX_ENABLED, "false");
            registrar.afterPropertiesSet();

            assertThat("BasicTestMetrics#bindTo should not have been executed if the registrar is disabled",
                    basicTestMetrics.getBindToExecutions(),
                    is(0));
        }

        @Test
        void enabled() {
            registrar.setEnabled(true);
            properties.put(MeterBinderRegistrar.PROP_BINDER_PREFIX + "stub-test-metrics"
                    + MeterBinderRegistrar.PROP_BINDER_SUFFIX_ENABLED, "true");
            registrar.afterPropertiesSet();

            assertThat("BasicTestMetrics#bindTo should be executed once if it's enabled",
                    basicTestMetrics.getBindToExecutions(),
                    is(1));
        }

    }

    @Nested
    class NamedMeterBinderTests {

        private static final String customMeterName = "ThIsiSaCustOmMetErName";
        private NamedTestMetrics namedTestMetrics;

        @BeforeEach
        void additionalSetup() {
            namedTestMetrics = new NamedTestMetrics();
            namedTestMetrics.setName(customMeterName);
            when(applicationContext.getBeansOfType(MeterBinder.class))
                    .thenReturn(Collections.singletonMap("testEventTriggeredMetrics", namedTestMetrics));
        }

        @Test
        void enabled() {
            // The default, class based behavior should have no impact:
            properties.put(MeterBinderRegistrar.PROP_BINDER_PREFIX + "named-test"
                    + MeterBinderRegistrar.PROP_BINDER_SUFFIX_ENABLED, "false");

            registrar.afterPropertiesSet();

            assertThat(namedTestMetrics.getBindToExecutions(), is(1));
        }

        @Test
        void disabled() {
            // The default, class based behavior should have no impact:
            properties.put(MeterBinderRegistrar.PROP_BINDER_PREFIX + customMeterName
                    + MeterBinderRegistrar.PROP_BINDER_SUFFIX_ENABLED, "false");

            registrar.afterPropertiesSet();

            assertThat(namedTestMetrics.getBindToExecutions(), is(0));
        }

    }

    @Nested
    class EventTriggeredMeterBinderTests {

        private TestEventTriggeredMetrics metrics;

        @BeforeEach
        void additionalSetup() {
            metrics = new TestEventTriggeredMetrics();
            when(applicationContext.getBeansOfType(MeterBinder.class))
                    .thenReturn(Collections.singletonMap("testEventTriggeredMetrics", metrics));
        }

        @Test
        void applicationEventTriggersBindTo() {
            registrar.afterPropertiesSet();

            assertThat("TestEventTriggeredMetrics#bindTo should be executed once if it's enabled",
                    metrics.getBindToExecutions(),
                    is(1));

            registrar.onApplicationEvent(new ClusterServiceInitialisedEvent(mock(ClusterService.class)));

            assertThat(
                    "TestEventTriggeredMetrics#bindTo should be executed again if the corresponding ApplicationEvent has happened",
                    metrics.getBindToExecutions(),
                    is(2));
        }

        @Test
        void onlyTriggeredOnEvent() {
            metrics.setTriggerOnStartup(false);
            registrar.afterPropertiesSet();

            assertThat("TestEventTriggeredMetrics#bindTo not have been executed on startup if this is disabled",
                    metrics.getBindToExecutions(),
                    is(0));

            registrar.onApplicationEvent(new ClusterServiceInitialisedEvent(mock(ClusterService.class)));

            assertThat(
                    "TestEventTriggeredMetrics#bindTo should be executed if the corresponding ApplicationEvent has happened",
                    metrics.getBindToExecutions(),
                    is(1));
        }

        @Test
        void dontTriggerIfDisabled() {
            properties.put(MeterBinderRegistrar.PROP_BINDER_PREFIX + "test-event-triggered"
                    + MeterBinderRegistrar.PROP_BINDER_SUFFIX_ENABLED, "false");

            registrar.afterPropertiesSet();

            assertThat("TestEventTriggeredMetrics#bindTo not have been executed on startup if disabled",
                    metrics.getBindToExecutions(),
                    is(0));

            registrar.onApplicationEvent(new ClusterServiceInitialisedEvent(mock(ClusterService.class)));

            assertThat("TestEventTriggeredMetrics#bindTo not have been executed on event if disabled",
                    metrics.getBindToExecutions(),
                    is(0));
        }

    }

}