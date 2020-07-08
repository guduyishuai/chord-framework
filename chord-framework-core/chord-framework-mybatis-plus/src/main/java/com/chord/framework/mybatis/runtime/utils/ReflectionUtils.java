package com.chord.framework.mybatis.runtime.utils;

import java.lang.reflect.Field;

/**
 * Created on 2020/6/10
 *
 * @author: wulinfeng
 */
public class ReflectionUtils {

    public static Field findField(Class<?> clazz, String name) {
        Class<?> searchType = clazz;
        while (Object.class != searchType && searchType != null) {
            Field[] fields = getDeclaredFields(searchType);
            for (Field field : fields) {
                if ((name == null || name.equals(field.getName()))) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    public static Field[] getDeclaredFields(Class<?> clazz) {
        Field[] result = null;
        try {
            result = clazz.getDeclaredFields();
        }
        catch (Throwable ex) {
            throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() +
                    "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
        }
        return result;
    }

}
