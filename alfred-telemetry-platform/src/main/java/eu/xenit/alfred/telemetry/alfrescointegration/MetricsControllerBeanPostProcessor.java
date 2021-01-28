package eu.xenit.alfred.telemetry.alfrescointegration;

import java.util.Properties;
import javax.annotation.Nonnull;
import org.alfresco.enterprise.metrics.MetricsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * {@link BeanDefinitionRegistryPostProcessor} which overwrites Alfresco's default ({@link MetricsController
 * metricsController} bean with the custom ({@link AlfredTelemetryMetricsController} implementation
 */
public class MetricsControllerBeanPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsControllerBeanPostProcessor.class);

    private static final String METER_REGISTRY_BEAN_ID = "meterRegistry";
    static final String METRICS_CONTROLLER_BEAN_ID = "metricsController";
    static final String METRICS_CONTROLLER_BEAN_ID_RENAMED = "alfred-telemetry.alfresco-metricsController";

    static final String PROP_KEY_INTEGRATION_ENABLED = "alfred.telemetry.alfresco-integration.enabled";
    static final String PROP_KEY_ENABLE_DEFAULT_REGISTRY = "alfred.telemetry.alfresco-integration.use-default-alfresco-registry";

    private final boolean alfrescoIntegrationEnabled;
    private final boolean enableDefaultAlfrescoRegistry;

    public MetricsControllerBeanPostProcessor(Properties globalProperties) {
        this.alfrescoIntegrationEnabled = Boolean
                .parseBoolean(globalProperties.getProperty(PROP_KEY_INTEGRATION_ENABLED));
        this.enableDefaultAlfrescoRegistry = Boolean
                .parseBoolean(globalProperties.getProperty(PROP_KEY_ENABLE_DEFAULT_REGISTRY));
    }

    @Override
    public void postProcessBeanDefinitionRegistry(@Nonnull BeanDefinitionRegistry registry) throws BeansException {

        if (!alfrescoIntegrationEnabled) {
            LOGGER.info(
                    "Alfred Telemetry integration with the default Alfresco metrics mechanism is currently disabled");
            return;
        }

        BeanDefinition originalMetricsControllerBeanDef;
        try {
            originalMetricsControllerBeanDef = registry.getBeanDefinition(METRICS_CONTROLLER_BEAN_ID);
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.info("Bean with id '" + METRICS_CONTROLLER_BEAN_ID + "' not found in the BeanDefinitionRegistry");
            return;
        }

        registry.registerBeanDefinition(METRICS_CONTROLLER_BEAN_ID_RENAMED, originalMetricsControllerBeanDef);
        LOGGER.info(
                "Renamed original '{}' bean to '{}'", METRICS_CONTROLLER_BEAN_ID, METRICS_CONTROLLER_BEAN_ID_RENAMED);

        GenericBeanDefinition alfredMetricsController = new GenericBeanDefinition();

        ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
        constructorArgumentValues.addGenericArgumentValue(new RuntimeBeanReference(METER_REGISTRY_BEAN_ID));
        constructorArgumentValues.addGenericArgumentValue(new RuntimeBeanReference(METRICS_CONTROLLER_BEAN_ID_RENAMED));
        constructorArgumentValues.addGenericArgumentValue(enableDefaultAlfrescoRegistry);

        alfredMetricsController.setBeanClass(AlfredTelemetryMetricsController.class);
        alfredMetricsController.setConstructorArgumentValues(constructorArgumentValues);
        // Workaround to make sure Alfresco's logging hierarchy is initialized before our bean so no logging is lost:
        alfredMetricsController.setDependsOn("log4JHierarchyInit");

        registry.registerBeanDefinition(METRICS_CONTROLLER_BEAN_ID, alfredMetricsController);
        LOGGER.info("Bean '{}' overwritten with custom '{}' implementation", METRICS_CONTROLLER_BEAN_ID,
                AlfredTelemetryMetricsController.class.getCanonicalName());

    }

    @Override
    public void postProcessBeanFactory(@Nonnull ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
