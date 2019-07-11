package eu.xenit.alfred.telemetry.binder.cache;

import io.micrometer.core.instrument.binder.MeterBinder;
import javax.annotation.Nonnull;
import org.alfresco.service.descriptor.DescriptorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class CacheMetricsFactory extends AbstractFactoryBean<MeterBinder> implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheMetricsFactory.class);

    private ApplicationContext ctx;
    private DescriptorService descriptorService;

    public CacheMetricsFactory(DescriptorService descriptorService) {
        this.descriptorService = descriptorService;
    }

    @Override
    public Class<?> getObjectType() {
        return MeterBinder.class;
    }

    @Override
    @Nonnull
    protected MeterBinder createInstance() {
        return isEnterpriseEdition()
                ? new EnterpriseCacheMetrics(ctx, descriptorService)
                : new CommunityCacheMetrics(ctx);
    }

    private boolean isEnterpriseEdition() {
        if (descriptorService == null || descriptorService.getServerDescriptor() == null) {
            LOGGER.debug("descriptorService is null, assuming ACE");
            return false;
        }

        final String edition = descriptorService.getServerDescriptor().getEdition();
        LOGGER.debug("Alfresco Edition: '{}'", edition);

        return "enterprise".equalsIgnoreCase(edition);
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}
