package eu.xenit.alfred.telemetry.binder.solr;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.Properties;
import org.alfresco.service.transaction.TransactionService;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

class SolrMetricsBeanPostProcessorTest {

    @Test
    public void testAllBeansCreated() {
        SolrMetricsBeanPostProcessor solrMetricsBeanPostProcessor = getSolrMetricsBeanPostProcessor(true, true);
        BeanDefinitionRegistry beanDefinitionRegistry = mock(BeanDefinitionRegistry.class);
        solrMetricsBeanPostProcessor.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
        verify(beanDefinitionRegistry)
                .registerBeanDefinition(eq(SolrMetricsBeanPostProcessor.SOLR_TRACKING_METRICS_BEAN_ID), any());
        verify(beanDefinitionRegistry)
                .registerBeanDefinition(eq(SolrMetricsBeanPostProcessor.SOLR_SHARDING_METRICS_BEAN_ID), any());
    }

    private SolrMetricsBeanPostProcessor getSolrMetricsBeanPostProcessor(boolean tracking, boolean sharding) {
        Properties properties = new Properties();
        properties.setProperty(SolrMetricsBeanPostProcessor.SOLR_TRACKING_METRICS_ENABLED_PROPERTY,
                Boolean.toString(tracking));
        properties.setProperty(SolrMetricsBeanPostProcessor.SOLR_SHARDING_METRICS_ENABLED_PROPERTY,
                Boolean.toString(sharding));
        SolrMetricsBeanPostProcessor solrMetricsBeanPostProcessor = new SolrMetricsBeanPostProcessor(properties,
                mock(MeterRegistry.class), mock(Scheduler.class), mock(TransactionService.class));
        return solrMetricsBeanPostProcessor;
    }

    @Test
    public void noBeansCreated() {
        BeanDefinitionRegistry beanDefinitionRegistry = mock(BeanDefinitionRegistry.class);
        getSolrMetricsBeanPostProcessor(false, false).postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
        verify(beanDefinitionRegistry, never())
                .registerBeanDefinition(eq(SolrMetricsBeanPostProcessor.SOLR_TRACKING_METRICS_BEAN_ID), any());
        verify(beanDefinitionRegistry, never())
                .registerBeanDefinition(eq(SolrMetricsBeanPostProcessor.SOLR_SHARDING_METRICS_BEAN_ID), any());
    }

    @Test
    public void checkAlfresco7BeanCreated() {
        BeanDefinitionRegistry beanDefinitionRegistry = mock(BeanDefinitionRegistry.class);
        when(beanDefinitionRegistry
                .getBeanDefinition(eq(SolrMetricsBeanPostProcessor.SEARCH_TRACKING_COMPONENT_BEAN_ID_BEFORE_7)))
                .thenThrow(
                        NoSuchBeanDefinitionException.class);
        getSolrMetricsBeanPostProcessor(true, false).postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
        verify(beanDefinitionRegistry)
                .registerBeanDefinition(eq(SolrMetricsBeanPostProcessor.SOLR_TRACKING_METRICS_BEAN_ID), any());
    }

}