package eu.xenit.alfred.telemetry.service;

import eu.xenit.alfred.telemetry.util.MicrometerModules;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

public class MicrometerOnClasspathDetector implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        if (!MicrometerModules.isMicrometerOnClasspath()) {
            throw new RuntimeException("Required dependency 'io.micrometer:micrometer-core' is not found on the classpath!");
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
