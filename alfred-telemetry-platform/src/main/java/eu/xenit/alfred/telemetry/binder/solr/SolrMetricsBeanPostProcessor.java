package eu.xenit.alfred.telemetry.binder.solr;

import eu.xenit.alfred.telemetry.binder.solr.sharding.SolrShardingMetricsFactory;
import eu.xenit.alfred.telemetry.binder.solr.tracking.SolrTrackingMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Properties;
import org.alfresco.service.transaction.TransactionService;
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

    public static final String SEARCH_TRACKING_COMPONENT_BEAN_ID_BEFORE_7 = "search.solrTrackingComponent";
    public static final String SEARCH_TRACKING_COMPONENT_BEAN_ID_AFTER_7 = "search.trackingComponent";
    public static final String SOLR_TRACKING_METRICS_ENABLED_PROPERTY = "alfred.telemetry.binder.solr.tracking.enabled";
    public static final String SOLR_TRACKING_METRICS_BEAN_ID = "eu.xenit.alfred.telemetry.binder.solr.tracking.SolrTrackingMetrics";
    public static final String SOLR_SHARDING_REGISTRY_BEAN_ID = "search.SolrShardRegistry";
    public static final String SOLR_SHARDING_METRICS_BEAN_ID = "eu.xenit.alfred.telemetry.binder.solr.sharding.SolrShardingMetrics";
    public static final String SOLR_SHARDING_METRICS_CRON_PROPERTY = "alfred.telemetry.binder.solr.sharding.cronexpression";
    public static final String SOLR_SHARDING_METRICS_ENABLED_PROPERTY = "alfred.telemetry.binder.solr.sharding.enabled";
    public static final String QUARTZ_CHECK_CLASS = "org.quartz.ScheduleBuilder";
    public static final String SOLR_SHARDING_METRICS_FLOC_ID_ENABLED_PROPERTY = "alfred.telemetry.binder.solr.sharding.floc.id.enabled";
    private final Scheduler scheduler;
    private final Properties globalProperties;

    private MeterRegistry meterRegistry;
    private TransactionService transactionService;

    public SolrMetricsBeanPostProcessor(Properties globalProperties, MeterRegistry meterRegistry, Scheduler scheduler, TransactionService transactionService) {
        this.globalProperties = globalProperties;
        this.meterRegistry = meterRegistry;
        this.scheduler = scheduler;
        this.transactionService = transactionService;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        createTrackingMetricsBean(beanDefinitionRegistry);
        createShardingMetricsBean(beanDefinitionRegistry);
    }

    private void createTrackingMetricsBean(BeanDefinitionRegistry beanDefinitionRegistry) {
        if (!Boolean.parseBoolean(globalProperties.getProperty(SOLR_TRACKING_METRICS_ENABLED_PROPERTY))) {
            LOGGER.info("Solr tracking metrics are not enabled, skipping.");
            return;
        }

        BeanDefinition solrTrackingComponentBean;
        try {
            //Fetching bean for Alfresco <7
            solrTrackingComponentBean = beanDefinitionRegistry.getBeanDefinition(
                    SEARCH_TRACKING_COMPONENT_BEAN_ID_BEFORE_7);
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.info(String.format("%s not found, trying %s", SEARCH_TRACKING_COMPONENT_BEAN_ID_BEFORE_7,
                    SEARCH_TRACKING_COMPONENT_BEAN_ID_AFTER_7));
            solrTrackingComponentBean = beanDefinitionRegistry.getBeanDefinition(
                    SEARCH_TRACKING_COMPONENT_BEAN_ID_AFTER_7);
        }
        GenericBeanDefinition solrTrackingMetricsBean = new GenericBeanDefinition();
        solrTrackingMetricsBean.setBeanClass(SolrTrackingMetrics.class);
        ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
        constructorArgumentValues.addGenericArgumentValue(solrTrackingComponentBean);
        constructorArgumentValues.addGenericArgumentValue(transactionService);
        constructorArgumentValues.addGenericArgumentValue(meterRegistry);
        solrTrackingMetricsBean.setConstructorArgumentValues(constructorArgumentValues);
        beanDefinitionRegistry.registerBeanDefinition(SOLR_TRACKING_METRICS_BEAN_ID, solrTrackingMetricsBean);
        LOGGER.info("Registered SolrTrackingMetrics bean");
    }

    private void createShardingMetricsBean(BeanDefinitionRegistry beanDefinitionRegistry) {
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
