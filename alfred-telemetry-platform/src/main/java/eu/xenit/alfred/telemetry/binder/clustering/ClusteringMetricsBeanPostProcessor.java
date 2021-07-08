package eu.xenit.alfred.telemetry.binder.clustering;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;

public class ClusteringMetricsBeanPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ClusteringMetricsBeanPostProcessor.class);

    public static final String CLUSTER_SERVICE = "clusterService";
    public static final String CLUSTERING_METRICS_ENABLED_PROPERTY = "alfred.telemetry.binder.clustering.enabled";
    public static final String CLUSTERING_METRICS_BEAN_ID = "eu.xenit.alfred.telemetry.binder.clustering.ClusteringMetrics";
    public static final String COMMUNITY_CLUSTERING_METRICS_BEAN_ID = "eu.xenit.alfred.telemetry.binder.clustering.CommunityClusteringMetrics";

    private final Properties globalProperties;

    public ClusteringMetricsBeanPostProcessor(Properties globalProperties) {
        this.globalProperties = globalProperties;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        createClusteringMetricsBean(beanDefinitionRegistry);
    }

    private void createClusteringMetricsBean(BeanDefinitionRegistry beanDefinitionRegistry) {
        BeanDefinition clusterServiceBean;
        try {
            clusterServiceBean = beanDefinitionRegistry.getBeanDefinition(CLUSTER_SERVICE);
            GenericBeanDefinition clusteringMetricsBean = new GenericBeanDefinition();
            clusteringMetricsBean.setBeanClass(ClusteringMetrics.class);
            ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
            constructorArgumentValues.addGenericArgumentValue(clusterServiceBean);
            clusteringMetricsBean.setConstructorArgumentValues(constructorArgumentValues);
            beanDefinitionRegistry.registerBeanDefinition(CLUSTERING_METRICS_BEAN_ID, clusteringMetricsBean);
            logger.info("Registered ClusteringMetrics bean");
        } catch (NoSuchBeanDefinitionException e) {
            logger.error(String.format("%s not found, this feature only works on Alfresco enterprise", CLUSTER_SERVICE));
            GenericBeanDefinition clusteringMetricsBean = new GenericBeanDefinition();
            clusteringMetricsBean.setBeanClass(CommunityClusteringMetrics.class);
            ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
            constructorArgumentValues.addGenericArgumentValue(globalProperties);
            clusteringMetricsBean.setConstructorArgumentValues(constructorArgumentValues);
            clusteringMetricsBean.setLazyInit(true);
            beanDefinitionRegistry.registerBeanDefinition(COMMUNITY_CLUSTERING_METRICS_BEAN_ID, clusteringMetricsBean);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // no need to modify the BeanFactory
    }
}
