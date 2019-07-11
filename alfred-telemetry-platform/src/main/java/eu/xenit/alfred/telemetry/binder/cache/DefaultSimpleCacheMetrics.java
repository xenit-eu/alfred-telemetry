package eu.xenit.alfred.telemetry.binder.cache;

import com.google.common.cache.Cache;
import eu.xenit.alfred.telemetry.util.ReflectionUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.cache.GuavaCacheMetrics;
import org.alfresco.repo.cache.DefaultSimpleCache;

class DefaultSimpleCacheMetrics extends GuavaCacheMetrics {

    static <C extends DefaultSimpleCache> C monitor(MeterRegistry registry, C cache, String cacheName,
            Iterable<Tag> tags) throws NoSuchFieldException, IllegalAccessException {
        new DefaultSimpleCacheMetrics(cache, cacheName, tags).bindTo(registry);
        return cache;
    }

    private DefaultSimpleCacheMetrics(DefaultSimpleCache cache, String cacheName,
            Iterable<Tag> tags) throws NoSuchFieldException, IllegalAccessException {
        super(extractRealCache(cache), cacheName, tags);
    }

    private static Cache extractRealCache(final DefaultSimpleCache cache)
            throws NoSuchFieldException, IllegalAccessException {
        return ReflectionUtil.extractField(cache, "cache");
    }
}
