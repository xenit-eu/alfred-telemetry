package eu.xenit.alfred.telemetry.util;

import eu.xenit.alfred.telemetry.binder.cache.EnterpriseCacheMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtil.class);

    private ReflectionUtil() {
        // private ctor to hide implicit public one
    }

    @SuppressWarnings("unchecked")
    public static <T> T extractField(final Object object, final String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        final Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }

    public static Method getMethod(Class<?> target, String methodName, Class<?>... parameterTypes) {
        try {
            return target.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            LOGGER.warn(e.getMessage());
        }
        return null;
    }
}
