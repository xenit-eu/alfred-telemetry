package eu.xenit.alfred.telemetry.binder.solr;

import eu.xenit.alfred.telemetry.binder.solr.sharding.SolrShardingMetrics;
import eu.xenit.alfred.telemetry.binder.solr.sharding.SolrShardingMetricsFactory;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Properties;
import org.quartz.Scheduler;
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

public class SolrMetricsBeanPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrMetricsBeanPostProcessor.class);

    public static final String SOLR_SHARDING_REGISTRY_BEAN_ID = "search.SolrShardRegistry";
    public static final String SOLR_SHARDING_METRICS_BEAN_ID = "eu.xenit.alfred.telemetry.binder.solr.sharding.SolrShardingMetrics";
    public static final String SOLR_SHARDING_METRICS_CRON_PROPERTY = "alfred.telemetry.binder.solr.sharding.cronexpression";
    public static final String SOLR_SHARDING_METRICS_ENABLED_PROPERTY = "alfred.telemetry.binder.solr.sharding.enabled";
    public static final String QUARTZ_CHECK_CLASS = "org.quartz.ScheduleBuilder";
    public static final String SOLR_SHARDING_METRICS_FLOC_ID_ENABLED_PROPERTY = "alfred.telemetry.binder.solr.sharding.floc.id.enabled";
    private final Scheduler scheduler;
    private final Properties globalProperties;

    private MeterRegistry meterRegistry;

    public SolrMetricsBeanPostProcessor(Properties globalProperties, MeterRegistry meterRegistry, Scheduler scheduler) {
        this.globalProperties = globalProperties;
        this.meterRegistry = meterRegistry;
        this.scheduler = scheduler;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        if (!Boolean.parseBoolean(globalProperties.getProperty(SOLR_SHARDING_METRICS_ENABLED_PROPERTY))) {
            LOGGER.info("Solr sharding metrics are not enabled, skipping.");
            return;
        }

        BeanDefinition solrShardingRegistryBean;
        try {
            solrShardingRegistryBean = beanDefinitionRegistry.getBeanDefinition(SOLR_SHARDING_REGISTRY_BEAN_ID);
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.info("Bean with id {} not found, sharding metrics will not be made available.", SOLR_SHARDING_REGISTRY_BEAN_ID);
            return;
        }

        try {
            Class.forName(QUARTZ_CHECK_CLASS);
        } catch (ClassNotFoundException e) {
            LOGGER.info("{} not found on classpath. Sharding metrics will not be made available.", QUARTZ_CHECK_CLASS);
            return;
        }

        GenericBeanDefinition solrShardingMetricsBean = new GenericBeanDefinition();
        ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
        // referencing the bean by name does not seem to work in this context
        // (constructorArgumentValues.addGenericArgumentValue(new RuntimeBeanReference(SOLR_SHARDING_REGISTRY_BEAN_ID));
        // I have no idea why, but it might have something to do with being in a subsystem context?
        constructorArgumentValues.addGenericArgumentValue(solrShardingRegistryBean);
        constructorArgumentValues.addGenericArgumentValue(meterRegistry);
        constructorArgumentValues.addGenericArgumentValue(scheduler);
        constructorArgumentValues.addGenericArgumentValue(globalProperties.get(SOLR_SHARDING_METRICS_CRON_PROPERTY));
        constructorArgumentValues.addGenericArgumentValue(Boolean.parseBoolean(globalProperties.getProperty(SOLR_SHARDING_METRICS_FLOC_ID_ENABLED_PROPERTY)));
        solrShardingMetricsBean.setConstructorArgumentValues(constructorArgumentValues);
        solrShardingMetricsBean.setDestroyMethodName("destroy");
        solrShardingMetricsBean.setBeanClass(SolrShardingMetricsFactory.class);
        beanDefinitionRegistry.registerBeanDefinition(SOLR_SHARDING_METRICS_BEAN_ID, solrShardingMetricsBean);
        LOGGER.info("Registered SolrShardingMetricsFactory bean");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // no need to modify the BeanFactory
    }
}
