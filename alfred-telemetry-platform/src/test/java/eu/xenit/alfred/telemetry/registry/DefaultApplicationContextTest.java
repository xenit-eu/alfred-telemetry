package eu.xenit.alfred.telemetry.registry;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import eu.xenit.alfred.telemetry.registry.graphite.GraphiteRegistryFactoryWrapper;
import eu.xenit.alfred.telemetry.registry.jmx.JmxRegistryFactoryWrapper;
import eu.xenit.alfred.telemetry.registry.prometheus.PrometheusRegistryFactoryWrapper;
import eu.xenit.alfred.telemetry.registry.simple.SimpleRegistryFactoryWrapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * Tests for the (default) behavior of the shipped XML & property configuration files.
 */
class DefaultApplicationContextTest {

    @Nested
    class SimpleMeterRegistryTest {

        @Test
        void enabledByDefault() {
            ApplicationContext applicationContext = new GenericXmlApplicationContext(
                    "test-global-properties-context.xml",
                    "alfresco/module/*/context/registry-context.xml"
            );

            SimpleRegistryFactoryWrapper wrapper = applicationContext.getBean(SimpleRegistryFactoryWrapper.class);
            assertThat(
                    "The 'SimplyMeterRegistry' should be enabled when using the default configuration",
                    wrapper.isRegistryEnabled(),
                    is(true));
        }

        @Test
        void canBeDisabled() {
            ApplicationContext applicationContext = new GenericXmlApplicationContext(
                    "test-global-properties-context.xml",
                    "alfresco/module/*/context/registry-context.xml",

                    // Include alfresco-global.properties sample for testing purposes
                    "include-test-properties-context.xml"
            );

            SimpleRegistryFactoryWrapper wrapper = applicationContext.getBean(SimpleRegistryFactoryWrapper.class);
            assertThat(
                    "It should be possible to enable the 'SimplyMeterRegistry' with alfresco-global.properties",
                    wrapper.isRegistryEnabled(),
                    is(false));
        }

    }

    @Nested
    class GraphiteMeterRegistryTest {

        @Test
        void enabledByDefault() {
            ApplicationContext applicationContext = new GenericXmlApplicationContext(
                    "test-global-properties-context.xml",
                    "alfresco/module/*/context/registry-context.xml"
            );

            GraphiteRegistryFactoryWrapper wrapper = applicationContext.getBean(GraphiteRegistryFactoryWrapper.class);
            assertThat(
                    "The 'GraphiteMeterRegistry' should be enabled when using the default configuration",
                    wrapper.isRegistryEnabled(),
                    is(true));
        }

        @Test
        void canBeDisabled() {
            ApplicationContext applicationContext = new GenericXmlApplicationContext(
                    "test-global-properties-context.xml",
                    "alfresco/module/*/context/registry-context.xml",

                    // Include alfresco-global.properties sample for testing purposes
                    "include-test-properties-context.xml"
            );

            GraphiteRegistryFactoryWrapper wrapper = applicationContext.getBean(GraphiteRegistryFactoryWrapper.class);
            assertThat(
                    "It should be possible to disable the 'GraphiteMeterRegistry' with alfresco-global.properties",
                    wrapper.isRegistryEnabled(),
                    is(false));
        }
    }

    @Nested
    class PrometheusMeterRegistryTest {

        @Test
        void enabledByDefault() {
            ApplicationContext applicationContext = new GenericXmlApplicationContext(
                    "test-global-properties-context.xml",
                    "alfresco/module/*/context/registry-context.xml"
            );

            PrometheusRegistryFactoryWrapper wrapper = applicationContext.getBean(PrometheusRegistryFactoryWrapper.class);
            assertThat(
                    "The 'PrometheusMeterRegistry' should be enabled when using the default configuration",
                    wrapper.isRegistryEnabled(),
                    is(true));
        }

        @Test
        void canBeDisabled() {
            ApplicationContext applicationContext = new GenericXmlApplicationContext(
                    "test-global-properties-context.xml",
                    "alfresco/module/*/context/registry-context.xml",

                    // Include alfresco-global.properties sample for testing purposes
                    "include-test-properties-context.xml"
            );

            PrometheusRegistryFactoryWrapper wrapper = applicationContext.getBean(PrometheusRegistryFactoryWrapper.class);
            assertThat(
                    "It should be possible to disable the 'PrometheusMeterRegistry' with alfresco-global.properties",
                    wrapper.isRegistryEnabled(),
                    is(false));
        }
    }

    @Nested
    class JmxMeterRegistryTest {

        @Test
        void enabledByDefault() {
            ApplicationContext applicationContext = new GenericXmlApplicationContext(
                    "test-global-properties-context.xml",
                    "alfresco/module/*/context/registry-context.xml"
            );

            JmxRegistryFactoryWrapper wrapper = applicationContext.getBean(JmxRegistryFactoryWrapper.class);
            assertThat(
                    "The 'JmxMeterRegistryTest' should be enabled when using the default configuration",
                    wrapper.isRegistryEnabled(),
                    is(true));
        }

        @Test
        void canBeDisabled() {
            ApplicationContext applicationContext = new GenericXmlApplicationContext(
                    "test-global-properties-context.xml",
                    "alfresco/module/*/context/registry-context.xml",

                    // Include alfresco-global.properties sample for testing purposes
                    "include-test-properties-context.xml"
            );

            JmxRegistryFactoryWrapper wrapper = applicationContext.getBean(JmxRegistryFactoryWrapper.class);
            assertThat(
                    "It should be possible to disable the JmxMeterRegistryTest with alfresco-global.properties",
                    wrapper.isRegistryEnabled(),
                    is(false));
        }
    }
}
