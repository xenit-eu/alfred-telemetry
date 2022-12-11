package eu.xenit.alfred.telemetry.alfrescointegration;

import org.apache.camel.spring.GenericBeansException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.util.function.Supplier;

import static eu.xenit.alfred.telemetry.alfrescointegration.DataSourceBeanPostProcessor.DATASOURCE_BEAN_ID;
import static eu.xenit.alfred.telemetry.alfrescointegration.DataSourceBeanPostProcessor.BASIC_DATASOURCE_BEAN_ID;
import static eu.xenit.alfred.telemetry.alfrescointegration.DataSourceBeanPostProcessor.BASE_DEFAULT_DATASOURCE_BEAN_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataSourceBeanPostProcessorTest {

    private BeanDefinitionRegistry beanDefinitionRegistry_alfresco6x = null;
    private ApplicationContext context_alfresco6x = null;
    private BeanDefinitionRegistry beanDefinitionRegistry_alfresco7x = null;
    private ApplicationContext context_alfresco7x = null;

    private BeanDefinitionRegistry beanDefinitionRegistry_null = null;
    private ApplicationContext context_null = null;

    @Test
    void postProcessBeanDefinitionRegistry_noBaseDefaultDataSourceBean() {
        setupNullBeans();
        BeanDefinitionRegistry beanDefinitionRegistry = beanDefinitionRegistry_null;
        validate(beanDefinitionRegistry_null, context_null,
                false, false, false);
    }

    @Test
    void postProcessBeanDefinitionRegistry_nullBaseDefaultDataSourceBeanClassName() {
        setupNullBeans();
        BeanDefinitionRegistry beanDefinitionRegistry = new SimpleBeanDefinitionRegistry();
        BeanDefinition mockBeanDefinition = mock(BeanDefinition.class);
        when(mockBeanDefinition.getBeanClassName()).thenReturn(null);
        beanDefinitionRegistry.registerBeanDefinition(BASE_DEFAULT_DATASOURCE_BEAN_ID, mockBeanDefinition);

        validate(beanDefinitionRegistry, context_null,
                false, false, true);
    }

    @Test
    void postProcessBeanDefinitionRegistry_unknownBaseDefaultDataSourceBean() {
        setupNullBeans();
        BeanDefinitionRegistry beanDefinitionRegistry = new SimpleBeanDefinitionRegistry();
        BeanDefinition notSupportedDataSourceBeanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(SimpleDriverDataSource.class)
                .getBeanDefinition();
        beanDefinitionRegistry
                .registerBeanDefinition(BASE_DEFAULT_DATASOURCE_BEAN_ID, notSupportedDataSourceBeanDefinition);

        validate(beanDefinitionRegistry, context_null,
                false, false, true);
    }

    @Test
    void postProcessBeanDefinitionRegistry_dbcpDataSourceBean() {
        setupAlfresco6Beans();
        validate(beanDefinitionRegistry_alfresco6x, context_alfresco6x,
                true, true, true);
    }

    @Test
    void postProcessBeanDefinitionRegistry_dbcp2DataSourceBean() {
        setupAlfresco7Beans();
        validate(beanDefinitionRegistry_alfresco7x, context_alfresco7x,
                true, true, true);

    }

    private void validate(BeanDefinitionRegistry registry, ApplicationContext context,
                          boolean hasDataSource, boolean hasBasicDataSource, boolean hasBaseDefaultDataSource) {
        BeanDefinitionRegistryPostProcessor postProcessor = createProcessor(context);
        postProcessor.postProcessBeanDefinitionRegistry(registry);

        if(hasDataSource) {
            assertNotNull(context.getBean(DATASOURCE_BEAN_ID));
        } else {
            assertThrows(BeansException.class, () -> context.getBean(DATASOURCE_BEAN_ID));
        }

        assertThat(registry.containsBeanDefinition(BASIC_DATASOURCE_BEAN_ID), is(hasBasicDataSource));
        if(hasBasicDataSource) {
            GenericBeanDefinition genericBeanDefinition =
                    (GenericBeanDefinition)registry.getBeanDefinition(BASIC_DATASOURCE_BEAN_ID);
            Supplier supplier = genericBeanDefinition.getInstanceSupplier();
            assertNotNull(supplier);
            assertNotNull(supplier.get());
        }

        assertThat(registry.containsBeanDefinition(BASE_DEFAULT_DATASOURCE_BEAN_ID), is(hasBaseDefaultDataSource));
    }

    private void setupAlfresco6Beans() {
        beanDefinitionRegistry_alfresco6x = new SimpleBeanDefinitionRegistry();
        BeanDefinition dbcpBeanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition("org.apache.commons.dbcp.BasicDataSource")
                .getBeanDefinition();
        beanDefinitionRegistry_alfresco6x.registerBeanDefinition(BASE_DEFAULT_DATASOURCE_BEAN_ID, dbcpBeanDefinition);

        context_alfresco6x = mock(ApplicationContext.class);
        when(context_alfresco6x.getBean(DATASOURCE_BEAN_ID)).thenReturn(new org.apache.commons.dbcp.BasicDataSource());
    }

    private void setupAlfresco7Beans() {
        beanDefinitionRegistry_alfresco7x = new SimpleBeanDefinitionRegistry();
        BeanDefinition dbcp2BeanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition("org.apache.commons.dbcp2.BasicDataSource")
                .getBeanDefinition();
        beanDefinitionRegistry_alfresco7x.registerBeanDefinition(BASE_DEFAULT_DATASOURCE_BEAN_ID, dbcp2BeanDefinition);

        context_alfresco7x = mock(ApplicationContext.class);
        when(context_alfresco7x.getBean(DATASOURCE_BEAN_ID)).thenReturn(new org.apache.commons.dbcp2.BasicDataSource());
    }

    private void setupNullBeans() {
        beanDefinitionRegistry_null = new SimpleBeanDefinitionRegistry();
        context_null = mock(ApplicationContext.class);
        when(context_null.getBean(DATASOURCE_BEAN_ID)).thenThrow(new GenericBeansException(DATASOURCE_BEAN_ID));
    }

    private DataSourceBeanPostProcessor createProcessor(ApplicationContext applicationContext) {
        DataSourceBeanPostProcessor processor = new DataSourceBeanPostProcessor();
        processor.setApplicationContext(applicationContext);
        return processor;
    }

}