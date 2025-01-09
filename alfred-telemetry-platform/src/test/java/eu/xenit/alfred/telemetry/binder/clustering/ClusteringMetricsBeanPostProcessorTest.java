package eu.xenit.alfred.telemetry.binder.clustering;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

class ClusteringMetricsBeanPostProcessorTest {

    ClusteringMetricsBeanPostProcessor clusteringMetricsBeanPostProcessor;

    @BeforeEach
    public void init() {
        clusteringMetricsBeanPostProcessor = new ClusteringMetricsBeanPostProcessor();
    }

    @Test
    void testEnterpriseAndClusteringMetricsEnabled() {
        BeanDefinitionRegistry beanDefinitionRegistry = mock(BeanDefinitionRegistry.class);
        clusteringMetricsBeanPostProcessor.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
        verify(beanDefinitionRegistry)
                .registerBeanDefinition(eq(ClusteringMetricsBeanPostProcessor.CLUSTERING_METRICS_BEAN_ID), any());
    }

    @Test
    void testCommunityAndClusteringMetricsEnabled() {
        BeanDefinitionRegistry beanDefinitionRegistry = mock(BeanDefinitionRegistry.class);
        when(beanDefinitionRegistry.getBeanDefinition(ClusteringMetricsBeanPostProcessor.CLUSTER_SERVICE))
                .thenThrow(new NoSuchBeanDefinitionException("Bean not found"));
        clusteringMetricsBeanPostProcessor.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
        verify(beanDefinitionRegistry, times(0))
                .registerBeanDefinition(eq(ClusteringMetricsBeanPostProcessor.CLUSTERING_METRICS_BEAN_ID), any());
    }
}