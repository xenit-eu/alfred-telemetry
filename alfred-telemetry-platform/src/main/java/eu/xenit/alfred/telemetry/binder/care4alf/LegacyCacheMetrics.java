package eu.xenit.alfred.telemetry.binder.care4alf;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import eu.xenit.alfred.telemetry.binder.EventTriggeredMeterBinder;
import eu.xenit.alfred.telemetry.binder.cache.Hazelcast2CacheMetrics;
import eu.xenit.alfred.telemetry.util.ReflectionUtil;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.TransactionalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

/**
 * Mostly copy pasta of the Care4Alf CachesMetrics class for legacy support.
 * <p>
 * Known issues (fixes are out of Alfred Telemetry scope):
 * <ul>
 * <li>
 * Alfresco put's enterprise caches (always / often?) behind a Proxy. In this case, {@code cache.getClass()} will result
 * in a proxy class instead of the actual cache class and the cache will not be monitored.
 * </li>
 * <li>
 * Doesn't work on Alfresco Community Edition because there are AEE imports. (Resulting in e.g. ClassNotFoundException)
 * </li>
 * <li>
 * Doesn't work on Alfresco 6 or higher due to incompatible changes in the Hazelcast library used. (Alfresco 5 =
 * Hazelcast 2.4, Alfresco 6 = Hazelcast 3)
 * </li>
 * </ul>
 */
public class LegacyCacheMetrics implements EventTriggeredMeterBinder, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyCacheMetrics.class);

    private static final String CLASS_NAME_CLUSTERSERVICEINITIALISEDEVENT =
            "org.alfresco.enterprise.repo.cluster.core.ClusterServiceInitialisedEvent";

    private Properties properties;
    private ApplicationContext ctx;

    public LegacyCacheMetrics(Properties properties) {
        this.properties = properties;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Class<? extends ApplicationEvent>> triggeringEvents() {
        try {
            return Collections.singletonList(
                    (Class<? extends ApplicationEvent>) Class.forName(CLASS_NAME_CLUSTERSERVICEINITIALISEDEVENT)
            );
        } catch (ClassNotFoundException e) {
            LOGGER.trace("{} not present in Alfresco Community Edition", CLASS_NAME_CLUSTERSERVICEINITIALISEDEVENT, e);
            return Collections.emptyList();
        }
    }

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {
        ctx.getBeansOfType(SimpleCache.class, false, false).entrySet().stream()
                .filter(e -> !(e.getValue() instanceof TransactionalCache))
                .forEach(e -> monitorCacheSafe(e.getKey(), e.getValue(), registry));
    }

    private void monitorCacheSafe(final String cacheName, final SimpleCache cache, final MeterRegistry registry) {
        try {
            monitorCache(cacheName, cache, registry);
        } catch (Throwable e) {
            LOGGER.warn("Unable to monitor cache '{}' of type '{}'", cacheName, cache.getClass().getCanonicalName(), e);
        }
    }

    private void monitorCache(final String cacheName, final SimpleCache cache, final MeterRegistry registry)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        monitorCacheProperties(cacheName, registry);

        String cacheType = cache.getClass().getName();
        Gauge.builder(buildKey(cacheName, "size"), cache, c -> (long) c.getKeys().size()).register(registry);
        if ("org.alfresco.repo.cache.DefaultSimpleCache".equals(cacheType)) {
            monitorDefaultSimpleCache(cacheName, (DefaultSimpleCache) cache, registry);
            Gauge.builder(buildKey(cacheName, "type"), null, o -> 1L).register(registry);
        } else if ("org.alfresco.enterprise.repo.cluster.cache.InvalidatingCache".equals(cacheType)) {
            monitorInvalidatingCache(cacheName, cache, registry);
            Gauge.builder(buildKey(cacheName, "type"), null, o -> 2L).register(registry);
        } else if ("org.alfresco.enterprise.repo.cluster.cache.HazelcastSimpleCache".equals(cacheType)) {
            // These metrics can be a bit less reliable than former cache stats
            monitorHazelcastSimpleCache(cacheName, cache, registry);
            Gauge.builder(buildKey(cacheName, "type"), null, o -> 3L).register(registry);
        } else {
            LOGGER.debug("Ignoring cache {} of type {}", cacheName, cacheType);
            Gauge.builder(buildKey(cacheName, "type"), null, o -> -1L).register(registry);
        }
    }

    private void monitorCacheProperties(final String cacheName, final MeterRegistry registry) {
        Gauge.builder(buildKey(cacheName, "maxItems"), properties, p -> {
            String maxItems = p.getProperty(buildKey(cacheName, "maxItems", false));
            return Long.valueOf(maxItems == null ? "-1" : maxItems);
        }).register(registry);
        Gauge.builder(buildKey(cacheName, "tx.maxItems"), properties, p -> {
            String txMaxItems = p.getProperty(buildKey(cacheName, "tx.maxItems", false));
            return Long.valueOf(txMaxItems == null ? "-1" : txMaxItems);
        }).register(registry);
        Gauge.builder(buildKey(cacheName, "statsEnabled"), properties, p -> {
            String statsEnabled = p.getProperty(buildKey(cacheName, "tx.statsEnabled", false));
            return (long) ("true".equals(statsEnabled) ? 1 : 0);
        }).register(registry);
    }

    private void monitorHazelcastSimpleCache(final String cacheName, final SimpleCache simpleCache,
            final MeterRegistry registry)
            throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final Object map = ReflectionUtil.extractField(simpleCache, "map");
        final Method mapStatMethod = map.getClass().getMethod("getLocalMapStats");
        final Object stats = mapStatMethod.invoke(map);

        Gauge.builder(buildKey(cacheName, "nbGets"), stats,
                s -> Hazelcast2CacheMetrics.extractMetricWithReflection(s, "getOperationStats", "getNumberOfGets"))
                .register(registry);
        Gauge.builder(buildKey(cacheName, "nbPuts"), stats,
                s -> Hazelcast2CacheMetrics.extractMetricWithReflection(s, "getOperationStats", "getNumberOfPuts"))
                .register(registry);
        Gauge.builder(buildKey(cacheName, "nbMiss"), stats,
                s -> -1L)
                .register(registry);
        Gauge.builder(buildKey(cacheName, "nbEvictions"), stats,
                s -> Hazelcast2CacheMetrics.extractMetricWithReflection(s, "getOperationStats", "getNumberOfRemoves"))
                .register(registry);
    }

    private void monitorInvalidatingCache(final String cacheName, final SimpleCache simpleCache,
            final MeterRegistry registry) throws NoSuchFieldException, IllegalAccessException {
        final DefaultSimpleCache realCache = ReflectionUtil.extractField(simpleCache, "cache");
        monitorDefaultSimpleCache(cacheName, realCache, registry);
    }

    private void monitorDefaultSimpleCache(final String cacheName, final DefaultSimpleCache cache,
            final MeterRegistry registry) throws IllegalAccessException {
        try {
            final Field cacheField = cache.getClass().getDeclaredField("cache");
            cacheField.setAccessible(true);
            final Object realCacheObject = cacheField.get(cache);
            try {
                final Cache realCache = (Cache) realCacheObject;
                final CacheStats stats = realCache.stats();

                Gauge.builder(buildKey(cacheName, "nbGets"), stats, CacheStats::requestCount).register(registry);
                Gauge.builder(buildKey(cacheName, "nbPuts"), stats, CacheStats::loadCount).register(registry);
                Gauge.builder(buildKey(cacheName, "nbHits"), stats, CacheStats::hitCount).register(registry);
                Gauge.builder(buildKey(cacheName, "nbMiss"), stats, CacheStats::missCount).register(registry);
                Gauge.builder(buildKey(cacheName, "nbEvictions"), stats, CacheStats::evictionCount).register(registry);
            } catch (ClassCastException cce) {
                LOGGER.warn("Exception while trying to cast cache , issue might be related to guava version :", cce);
            }
        } catch (NoSuchFieldException | NoClassDefFoundError e) {
            // This field got introduced in Alfresco 5.x, ignore this exception
            LOGGER.debug("Skipping cache statistics collection: unsopported Alfresco version");
        }
    }

    private String buildKey(String cacheName, String key) {
        return buildKey(cacheName, key, true);
    }

    private String buildKey(String cacheName, String key, boolean sanitizeCacheName) {
        return "cache." + (sanitizeCacheName ? cacheName.replace('.', '-') : cacheName) + "." + key;
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}
