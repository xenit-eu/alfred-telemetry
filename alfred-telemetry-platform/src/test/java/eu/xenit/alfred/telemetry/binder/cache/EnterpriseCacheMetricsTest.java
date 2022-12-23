package eu.xenit.alfred.telemetry.binder.cache;

import com.hazelcast.core.IMap;
import eu.xenit.alfred.telemetry.util.ReflectionUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.cache.HazelcastCacheMetrics;
import org.alfresco.enterprise.repo.cluster.cache.HazelcastSimpleCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnterpriseCacheMetricsTest {
    ApplicationContext context;
    MeterRegistry meterRegistry;
    EnterpriseCacheMetrics enterpriseCacheMetrics;
    HazelcastSimpleCache hazelcastSimpleCache;
    IMap iMap;
    Method method;

    @BeforeEach
    void setup() {
        context = mock(ApplicationContext.class);
        meterRegistry = mock(MeterRegistry.class);
        iMap = mock(IMap.class);
        method = mock(Method.class);
        hazelcastSimpleCache = new HazelcastSimpleCache(iMap);
        enterpriseCacheMetrics = new EnterpriseCacheMetrics(context);

        when(context.getBeansOfType(org.alfresco.repo.cache.SimpleCache.class,
                false, false))
                .thenReturn(Map.of("test", hazelcastSimpleCache));
    }

    @Test
    void testMonitorCacheWithIMapReflection() throws InvocationTargetException, IllegalAccessException {
        try (MockedStatic<ReflectionUtil> mock =
                     Mockito.mockStatic(ReflectionUtil.class, Mockito.CALLS_REAL_METHODS)) {
            mock.when(() -> ReflectionUtil.getMethod(
                            HazelcastCacheMetrics.class,
                            "monitor",
                            MeterRegistry.class,
                            Object.class,
                            Iterable.class))
                    .thenReturn(null);
            mock.when(() -> ReflectionUtil.getMethod(
                            HazelcastCacheMetrics.class,
                            "monitor",
                            MeterRegistry.class,
                            IMap.class,
                            Iterable.class))
                    .thenReturn(method);
            enterpriseCacheMetrics.bindTo(meterRegistry);
            verify(method, times(1)).invoke(null, meterRegistry, iMap, Tags.of("type", "HazelcastSimpleCache"));
        }
    }

    @Test
    void testMonitorCacheObjectReflection() throws InvocationTargetException, IllegalAccessException {
        try (MockedStatic<ReflectionUtil> mock =
                     Mockito.mockStatic(ReflectionUtil.class, Mockito.CALLS_REAL_METHODS)) {
            mock.when(() -> ReflectionUtil.getMethod(
                            HazelcastCacheMetrics.class,
                            "monitor",
                            MeterRegistry.class,
                            Object.class,
                            Iterable.class))
                    .thenReturn(method);
            enterpriseCacheMetrics.bindTo(meterRegistry);
            verify(method, times(1)).invoke(null, meterRegistry, iMap, Tags.of("type", "HazelcastSimpleCache"));
        }
    }

    @Test
    void testMonitorCacheNoReflection() throws InvocationTargetException, IllegalAccessException {
        try (MockedStatic<ReflectionUtil> mock =
                     Mockito.mockStatic(ReflectionUtil.class, Mockito.CALLS_REAL_METHODS)) {
            mock.when(() -> ReflectionUtil.getMethod(
                            HazelcastCacheMetrics.class,
                            "monitor",
                            MeterRegistry.class,
                            Object.class,
                            Iterable.class))
                    .thenReturn(null);
            mock.when(() -> ReflectionUtil.getMethod(
                            HazelcastCacheMetrics.class,
                            "monitor",
                            MeterRegistry.class,
                            IMap.class,
                            Iterable.class))
                    .thenReturn(null);

            enterpriseCacheMetrics.bindTo(meterRegistry);
            verify(method, times(0)).invoke(null, meterRegistry, iMap, Tags.of("type", "HazelcastSimpleCache"));
        }
    }
}
