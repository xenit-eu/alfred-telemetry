package eu.xenit.alfred.telemetry.binder.cache;

import eu.xenit.alfred.telemetry.binder.NamedMeterBinder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.lang.NonNull;
import java.lang.reflect.Proxy;
import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.cache.SimpleCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class CommunityCacheMetrics implements NamedMeterBinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityCacheMetrics.class);

    private ApplicationContext ctx;

    CommunityCacheMetrics(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String getName() {
        return "cache";
    }

    @Override
    public void bindTo(@NonNull MeterRegistry registry) {
        ctx.getBeansOfType(SimpleCache.class, false, false)
                .forEach((key, value) -> monitorCache(key, value, registry));
    }

    static void monitorCache(final String cacheBeanName, SimpleCache cache, final MeterRegistry registry) {
        try {
            if (DefaultSimpleCache.class.equals(cache.getClass())) {
                DefaultSimpleCacheMetrics
                        .monitor(registry, (DefaultSimpleCache) cache, cacheBeanName,
                                Tags.of(Tag.of("type", cache.getClass().getSimpleName())));
                return;
            }

            if (Proxy.isProxyClass(cache.getClass())) {
                LOGGER.warn("Cache '{}' is a proxy, which is unexpected in Alfresco Community Edition", cacheBeanName);
            }

            LOGGER.debug("Cache '{}' of type '{}' not monitored", cacheBeanName, cache.getClass().getCanonicalName());

        } catch (Throwable e) {
            LOGGER.warn("Failed to monitor cache '{}' of type '{}'",
                    cacheBeanName, cache.getClass().getCanonicalName(), e);
        }
    }
}
