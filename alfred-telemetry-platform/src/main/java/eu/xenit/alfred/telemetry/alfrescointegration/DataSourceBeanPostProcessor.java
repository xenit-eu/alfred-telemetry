package eu.xenit.alfred.telemetry.alfrescointegration;

import eu.xenit.alfred.telemetry.binder.dbcp.TelemetryBasicDataSource;
import eu.xenit.alfred.telemetry.binder.dbcp.Dbcp2BasicDataSource;
import eu.xenit.alfred.telemetry.binder.dbcp.DbcpBasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.sql.DataSource;

public class DataSourceBeanPostProcessor implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceBeanPostProcessor.class);

    private static final String DATASOURCE_BEAN_ID = "dataSource";
    private static final String BASE_DEFAULT_DATASOURCE_BEAN_ID = "baseDefaultDataSource";
    private static final String BASIC_DATASOURCE_BEAN_ID = "versionSpecificBasicDataSource";
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // NOOP
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        BeanDefinition basicDataSourceBeanDefinition = getVersionSpecificBean(registry);
        if(basicDataSourceBeanDefinition != null) {
            LOGGER.debug("Setting basic data source bean...");
            registry.registerBeanDefinition(BASIC_DATASOURCE_BEAN_ID, basicDataSourceBeanDefinition);
        }
    }

    private BeanDefinition getVersionSpecificBean(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = getVersionSpecificBeanDefinitionBuilder(registry);
        return builder != null
            ? builder
                .setLazyInit(true)
                .addDependsOn("defaultDataSource")
                .getBeanDefinition()
            : null;
    }

    private BeanDefinitionBuilder getVersionSpecificBeanDefinitionBuilder(BeanDefinitionRegistry registry) {
        if(!registry.containsBeanDefinition(BASE_DEFAULT_DATASOURCE_BEAN_ID)){
            LOGGER.debug("Bean with name 'dataSource' not found! DataSource metrics will not autowire.");
            return null;
        }

        BeanDefinition dataSourceBean = registry.getBeanDefinition(BASE_DEFAULT_DATASOURCE_BEAN_ID);
        String className = dataSourceBean.getBeanClassName();
        if(className == null){
            LOGGER.debug("Bean with name 'dataSource' has 'null' class name! DataSource metrics will not autowire.");
            return null;
        }

        LOGGER.debug("Bean ID '{}' is of type '{}'", BASE_DEFAULT_DATASOURCE_BEAN_ID, className);
        if(!className.contains("dbcp")) {
            return null;
        }

        return BeanDefinitionBuilder
                .genericBeanDefinition(
                        TelemetryBasicDataSource.class,
                        (
                            className.contains("dbcp2")
                                ? () -> new Dbcp2BasicDataSource(getDataSource())
                                : () -> new DbcpBasicDataSource(getDataSource())
                        )
                );
    }

    private DataSource getDataSource() {
        return (DataSource)applicationContext.getBean(DATASOURCE_BEAN_ID);
    }
}
