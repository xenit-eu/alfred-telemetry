package eu.xenit.alfred.telemetry.config;

import static eu.xenit.alfred.telemetry.config.CommonTagFilterFactory.PROP_KEY_PREFIX_COMMONTAG;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

class CommonTagFilterFactoryTest {

    private MeterFilter initializeMeterFilter(Properties properties) {
        ApplicationContext context = new AnnotationConfigApplicationContext(CommonTagFilterFactoryConfig.class);
        context.getBean("global-properties", Properties.class).putAll(properties);
        return context.getBean(MeterFilter.class);
    }

    @Test
    void commonTag_defaultTags() {
        final MeterFilter filter = initializeMeterFilter(new Properties());

        final Id originalId = new Id("name", Tags.empty(), null, null, Type.COUNTER);
        final Id mappedId = filter.map(originalId);
        assertThat(mappedId.getTag("application"), is(equalTo("alfresco")));
        assertThat(mappedId.getTag("host"), is(not(nullValue())));
    }

    @Test
    void commonTag_fromProperties() {
        final Properties properties = new Properties();
        properties.put(PROP_KEY_PREFIX_COMMONTAG + "tag.test-key", "test-value");
        properties.put("alfred.telemetry.bindertypo.common-tag." + "incorrect-key", "incorrect-value");
        final MeterFilter filter = initializeMeterFilter(properties);

        final Id originalId = new Id("name", Tags.empty(), null, null, Type.COUNTER);
        final Id mappedId = filter.map(originalId);
        assertThat(mappedId.getTag("tag.test-key"), is(equalTo("test-value")));
        assertThat(mappedId.getTag("incorrect-key"), is(nullValue()));
    }

    @Configuration
    static class CommonTagFilterFactoryConfig {

        @Bean("global-properties")
        Properties properties() {
            return new Properties();
        }

        @Bean
        @Lazy // make sure we can insert properties before actually creating the MeterFilter
        @SuppressWarnings("unused")
        public FactoryBean<MeterFilter> commonTagFilterFactory() {
            return new CommonTagFilterFactory(properties(), null);
        }
    }
}