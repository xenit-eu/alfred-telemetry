package eu.xenit.alfred.telemetry.alfrescointegration;

import static eu.xenit.alfred.telemetry.alfrescointegration.MetricsControllerBeanPostProcessor.METRICS_CONTROLLER_BEAN_ID;
import static eu.xenit.alfred.telemetry.alfrescointegration.MetricsControllerBeanPostProcessor.METRICS_CONTROLLER_BEAN_ID_RENAMED;
import static eu.xenit.alfred.telemetry.alfrescointegration.MetricsControllerBeanPostProcessor.PROP_KEY_ENABLE_DEFAULT_REGISTRY;
import static eu.xenit.alfred.telemetry.alfrescointegration.MetricsControllerBeanPostProcessor.PROP_KEY_INTEGRATION_ENABLED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Properties;
import org.alfresco.enterprise.metrics.MetricsControllerPrometheusImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;

class MetricsControllerBeanPostProcessorTest {

    private MetricsControllerBeanPostProcessor initPostProcessor(boolean integrationEnabled,
            boolean useDefaultAlfrescoRegistry) {
        final Properties globalProperties = new Properties();
        globalProperties.put(PROP_KEY_INTEGRATION_ENABLED, Boolean.toString(integrationEnabled));
        globalProperties.put(PROP_KEY_ENABLE_DEFAULT_REGISTRY, Boolean.toString(useDefaultAlfrescoRegistry));
        return new MetricsControllerBeanPostProcessor(globalProperties);
    }

    @Test
    void postProcessBeanDefinitionRegistry_doesntContainMetricsController() {
        BeanDefinitionRegistry beanDefinitionRegistry = new SimpleBeanDefinitionRegistry();

        BeanDefinitionRegistryPostProcessor postProcessor = initPostProcessor(true, true);
        postProcessor.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);

        assertThat(beanDefinitionRegistry.containsBeanDefinition(METRICS_CONTROLLER_BEAN_ID), is(false));
        assertThat(beanDefinitionRegistry.containsBeanDefinition(METRICS_CONTROLLER_BEAN_ID_RENAMED), is(false));
    }

    @Test
    void postProcessBeanDefinitionRegistry_overwritesMetricsController() {
        BeanDefinitionRegistry beanDefinitionRegistry = new SimpleBeanDefinitionRegistry();
        GenericBeanDefinition alfrescoMetricsControllerBean = new GenericBeanDefinition();
        alfrescoMetricsControllerBean.setBeanClass(MetricsControllerPrometheusImpl.class);
        beanDefinitionRegistry.registerBeanDefinition(METRICS_CONTROLLER_BEAN_ID, alfrescoMetricsControllerBean);

        BeanDefinitionRegistryPostProcessor postProcessor = initPostProcessor(true, true);
        postProcessor.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);

        assertThat(beanDefinitionRegistry.containsBeanDefinition(METRICS_CONTROLLER_BEAN_ID_RENAMED), is(true));
        assertThat(
                "Alfresco's original metricsController bean should have been renamed",
                beanDefinitionRegistry.getBeanDefinition(METRICS_CONTROLLER_BEAN_ID_RENAMED),
                is(alfrescoMetricsControllerBean));

        assertThat(
                beanDefinitionRegistry.containsBeanDefinition(METRICS_CONTROLLER_BEAN_ID), is(true));
        BeanDefinition metricsControllerBeanAfterPostProcess =
                beanDefinitionRegistry.getBeanDefinition(METRICS_CONTROLLER_BEAN_ID);
        assertThat(
                "metricsController should be our implementation after the post processing is done",
                metricsControllerBeanAfterPostProcess.getBeanClassName(),
                is(AlfredTelemetryMetricsController.class.getName()));
    }

    /**
     * Example scenario: Alfresco Micrometer mechanism is present (e.g. Alfresco EE 6.1) but the Alfred Telemetry
     * Alfresco integration is disabled.
     */
    @Test
    void postProcessBeanDefinitionRegistry_integrationDisabled() {
        BeanDefinitionRegistry beanDefinitionRegistry = new SimpleBeanDefinitionRegistry();
        GenericBeanDefinition alfrescoMetricsControllerBean = new GenericBeanDefinition();
        alfrescoMetricsControllerBean.setBeanClass(MetricsControllerPrometheusImpl.class);
        beanDefinitionRegistry.registerBeanDefinition(METRICS_CONTROLLER_BEAN_ID, alfrescoMetricsControllerBean);

        BeanDefinitionRegistryPostProcessor postProcessor = initPostProcessor(false, true);
        postProcessor.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);

        assertThat(beanDefinitionRegistry.containsBeanDefinition(METRICS_CONTROLLER_BEAN_ID_RENAMED), is(false));
        assertThat(beanDefinitionRegistry.containsBeanDefinition(METRICS_CONTROLLER_BEAN_ID), is(true));
        assertThat(
                "Alfresco's original metricsController bean should not have been touched if the post processor is disabled",
                beanDefinitionRegistry.getBeanDefinition(METRICS_CONTROLLER_BEAN_ID),
                is(alfrescoMetricsControllerBean));
    }

}