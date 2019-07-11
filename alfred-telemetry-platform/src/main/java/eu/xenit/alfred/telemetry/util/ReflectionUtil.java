package eu.xenit.alfred.telemetry.util;

import java.lang.reflect.Field;

public class ReflectionUtil {

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

}
