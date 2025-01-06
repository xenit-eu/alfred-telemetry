package eu.xenit.alfred.telemetry.binder.cache;

import com.hazelcast.map.IMap;
import eu.xenit.alfred.telemetry.binder.EventTriggeredMeterBinder;
import eu.xenit.alfred.telemetry.binder.NamedMeterBinder;
import eu.xenit.alfred.telemetry.util.ReflectionUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.cache.HazelcastCacheMetrics;
import org.alfresco.enterprise.repo.cluster.cache.HazelcastSimpleCache;
import org.alfresco.enterprise.repo.cluster.cache.InvalidatingCache;
import org.alfresco.enterprise.repo.cluster.core.ClusterServiceInitialisedEvent;
import org.alfresco.enterprise.repo.cluster.core.ClusteredObjectProxyFactory.ClusteredObjectProxyInvoker;
import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.cache.SimpleCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

public class EnterpriseCacheMetrics implements EventTriggeredMeterBinder, NamedMeterBinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnterpriseCacheMetrics.class);

    private ApplicationContext ctx;


    EnterpriseCacheMetrics(ApplicationContext ctx
    ) {
        this.ctx = ctx;

    }

    @Override
    public String getName() {
        return "cache";
    }

    @Override
    public List<Class<? extends ApplicationEvent>> triggeringEvents() {
        return Collections.singletonList(ClusterServiceInitialisedEvent.class);
    }

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {
        ctx.getBeansOfType(SimpleCache.class, false, false)
                .forEach((key, value) -> this.monitorCache(key, value, registry));
    }

    private void monitorCache(final String cacheBeanName, SimpleCache cache, final MeterRegistry registry) {
        try {
            cache = extractCacheIfProxy(cacheBeanName, cache);

            if (InvalidatingCache.class.equals(cache.getClass())) {
                monitorCache(cacheBeanName, (InvalidatingCache) cache, registry);
                return;
            }
            if (HazelcastSimpleCache.class.equals(cache.getClass())) {
                monitorCache((HazelcastSimpleCache) cache, registry);
                return;
            }

            // Pass to CommunityCacheMetrics for further processing
            CommunityCacheMetrics.monitorCache(cacheBeanName, cache, registry);

        } catch (Throwable e) {
            LOGGER.warn("Failed to monitor cache '{}' of type '{}'",
                    cacheBeanName, cache.getClass().getCanonicalName(), e);
        }
    }

    /* Private methods */

    private SimpleCache extractCacheIfProxy(final String cacheBeanName, final SimpleCache object) {
        if (!Proxy.isProxyClass(object.getClass())) {
            return object;
        }

        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(object);

        if (!(invocationHandler instanceof ClusteredObjectProxyInvoker)) {
            LOGGER.warn(
                    "Cache '{}': cannot resolve proxy because invocation handler" +
                            " of type '{}' is not an instance of 'ClusteredObjectProxyInvoker'",
                    cacheBeanName, invocationHandler.getClass());
            return object;
        }

        return (SimpleCache) ((ClusteredObjectProxyInvoker) invocationHandler).getBackingObject();
    }

    private void monitorCache(final HazelcastSimpleCache cache, final MeterRegistry registry)
            throws NoSuchFieldException, IllegalAccessException {
        IMap<?, ?> cacheMap = ReflectionUtil.extractField(cache, "map");
        Method method =
                ReflectionUtil.getMethod(
                        HazelcastCacheMetrics.class,
                        "monitor",
                        MeterRegistry.class,
                        Object.class,
                        Iterable.class
                );
        if (method == null) {
            method = ReflectionUtil.getMethod(
                    HazelcastCacheMetrics.class,
                    "monitor",
                    MeterRegistry.class,
                    IMap.class,
                    Iterable.class
            );
        }
        if (method == null) {
            LOGGER.warn("could not acquire HazelcastCacheMetrics.monitor method, {} will not be monitored",
                    cache.getClass().getSimpleName());
            return;
        }
        try {
            method.invoke(null, registry, cacheMap, Tags.of("type", cache.getClass().getSimpleName()));
        } catch (InvocationTargetException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }


    private void monitorCache(final String cacheBeanName, final InvalidatingCache cache, final MeterRegistry registry)
            throws NoSuchFieldException, IllegalAccessException {
        DefaultSimpleCache realCache = ReflectionUtil.extractField(cache, "cache");
        DefaultSimpleCacheMetrics.monitor(registry, realCache, cacheBeanName,
                Tags.of(Tag.of("type", cache.getClass().getSimpleName())));
    }
}
