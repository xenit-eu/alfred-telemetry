package eu.xenit.alfred.telemetry.config;

import static eu.xenit.alfred.telemetry.config.CommonTagFilterFactory.PROP_KEY_EXPORT_PROMETHEUS;
import static eu.xenit.alfred.telemetry.config.CommonTagFilterFactory.PROP_KEY_PREFIX_COMMONTAG;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import java.util.Properties;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.alfresco.enterprise.metrics.MetricsControllerPrometheusImpl;
import org.alfresco.enterprise.metrics.MetricsRegistryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

class CommonTagFilterFactoryTest {

    private MeterFilter initializeMeterFilter(Properties properties) {
        ApplicationContext context = new AnnotationConfigApplicationContext(CommonTagFilterFactoryConfig.class);
        context.getBean("global-properties", Properties.class).putAll(properties);
        return context.getBean(MeterFilter.class);
    }

    @Test
    void commonTag_defaultTags() {
        final MeterFilter filter = initializeMeterFilter(new Properties());

        final Id originalId = new Id("name", Tags.empty(), null, null, Type.COUNTER);
        final Id mappedId = filter.map(originalId);
        assertThat(mappedId.getTag("application"), is(equalTo("alfresco")));
        assertThat(mappedId.getTag("host"), is(not(nullValue())));
    }

    @Test
    void commonTag_fromProperties() {
        final Properties properties = new Properties();
        properties.put(PROP_KEY_PREFIX_COMMONTAG + "tag.test-key", "test-value");
        properties.put("alfred.telemetry.bindertypo.common-tag." + "incorrect-key", "incorrect-value");
        final MeterFilter filter = initializeMeterFilter(properties);

        final Id originalId = new Id("name", Tags.empty(), null, null, Type.COUNTER);
        final Id mappedId = filter.map(originalId);
        assertThat(mappedId.getTag("tag.test-key"), is(equalTo("test-value")));
        assertThat(mappedId.getTag("incorrect-key"), is(nullValue()));
    }

    @Test
    void commonTag_nullArguments() {
        assertNotNull(new CommonTagFilterFactory(null, null).createInstance());
    }

    @Test
    void commonTag_alfrescoPrometheusRegistry_nullPrometheusRegistry() throws Exception {
        assertNotNull(
            new CommonTagFilterFactory(null, createPrometheusController(null))
                .createInstance()
        );
    }

    @Test
    void commonTag_alfrescoPrometheusRegistry_nullGlobalProperties() throws Exception {
        validate(null, 0);
    }

    @Test
    void commonTag_alfrescoPrometheusRegistry_exportPrometheusNotSet() throws Exception {
        validate(new Properties(), 0);
    }

    @Test
    void commonTag_alfrescoPrometheusRegistry_exportPrometheusFalse() throws Exception {
        Properties properties = new Properties();
        properties.put(PROP_KEY_EXPORT_PROMETHEUS, "false");
        validate(properties, 0);
    }

    @Test
    void commonTag_alfrescoPrometheusRegistry_exportPrometheusTrue() throws Exception {
        Properties properties = new Properties();
        properties.put(PROP_KEY_EXPORT_PROMETHEUS, "true");
        validate(properties, 1);
    }

    private void validate(Properties properties, int expectedTimesCalled) throws Exception {
        MeterRegistry.Config config = mock(MeterRegistry.Config.class);
        when(config.commonTags(anyCollection())).thenReturn(null);

        PrometheusMeterRegistry registry = mock(PrometheusMeterRegistry.class);
        when(registry.config()).thenReturn(config);

        MetricsControllerPrometheusImpl prometheusController = createPrometheusController(registry);

        new CommonTagFilterFactory(properties, prometheusController);
        verify(config, times(expectedTimesCalled)).commonTags(anyCollection());
    }

    private MetricsControllerPrometheusImpl createPrometheusController(PrometheusMeterRegistry registry) throws Exception {
        MetricsRegistryFactory prometheusRegistryFactoryMock = mock(MetricsRegistryFactory.class);
        when(prometheusRegistryFactoryMock.createPrometheusRegistry()).thenReturn(registry);

        MetricsControllerPrometheusImpl prometheusController = new MetricsControllerPrometheusImpl();
        prometheusController.setMetricsRegistryFactory(prometheusRegistryFactoryMock);
        prometheusController.setEnabled(true);
        prometheusController.afterPropertiesSet();
        return prometheusController;
    }

    @Configuration
    static class CommonTagFilterFactoryConfig {

        @Bean("global-properties")
        Properties properties() {
            return new Properties();
        }

        @Bean
        @Lazy // make sure we can insert properties before actually creating the MeterFilter
        @SuppressWarnings("unused")
        public FactoryBean<MeterFilter> commonTagFilterFactory() {
            return new CommonTagFilterFactory(properties(), null);
        }
    }
}