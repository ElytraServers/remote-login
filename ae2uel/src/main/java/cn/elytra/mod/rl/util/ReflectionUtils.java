package cn.elytra.mod.rl.util;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class ReflectionUtils {

    private static final Map<Class<?>, ClassReflections> ClassReflectionCache = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object object, String fieldName) throws IllegalAccessException {
        // if the object is class, we treat the field as static
        boolean objectIsClass = object instanceof Class<?>;
        Class<?> clazz = objectIsClass ? (Class<?>) object : object.getClass();
        ClassReflections cr = ClassReflectionCache.computeIfAbsent(clazz, ClassReflections::new);
        Field f = cr.getFieldAccessible(fieldName);
        if(f == null) throw new IllegalStateException(fieldName + " not found");
        return (T) f.get(objectIsClass ? null : object);
    }

    public static <T> T getFieldValueSafe(Object object, String fieldName, T defaultValue) {
        try {
            return getFieldValue(object, fieldName);
        } catch(IllegalAccessException ignored) {
            return defaultValue;
        }
    }

    public static class ClassReflections {

        public final Class<?> clazz;

        protected final Map<Pair<String, Class<?>[]>, Method> methodCache = Maps.newHashMap();
        protected final Map<String, Field> fieldCache = Maps.newHashMap();

        public ClassReflections(Class<?> clazz) {
            this.clazz = clazz;
        }

        /**
         * Find the method with given information. The method is directly returned, so you'll need to check accessibility later.
         *
         * @param name           the method name
         * @param parameterTypes the method parameter types
         * @return the method or {@code null} if not found.
         */
        @Nullable
        public Method getMethod(String name, Class<?>... parameterTypes) {
            Pair<String, Class<?>[]> key = new ImmutablePair<>(name, parameterTypes);
            if(methodCache.containsKey(key)) {
                return methodCache.get(key);
            }

            try {
                // find the method in the class
                Method m = this.clazz.getDeclaredMethod(name, parameterTypes);
                methodCache.put(key, m);
                return m;
            } catch(NoSuchMethodException e) {
                // find the method in parent classes
                Class<?> superClass = this.clazz.getSuperclass();
                while(superClass != Object.class) {
                    try {
                        Method m = superClass.getDeclaredMethod(name, parameterTypes);
                        methodCache.put(key, m);
                        return m;
                    } catch(NoSuchMethodException ignored) {
                        superClass = superClass.getSuperclass();
                    }
                }

                // not found in any parent class :(
                return null;
            }
        }

        /**
         * Find the method with given information, and set the method accessible.
         *
         * @param name           the method name
         * @param parameterTypes the method parameter types
         * @return the method or {@code null} if not found.
         */
        @Nullable
        public Method getMethodAccessible(String name, Class<?>... parameterTypes) {
            Method m = getMethod(name, parameterTypes);
            if(m == null) {
                return null;
            }
            if(!m.isAccessible()) {
                m.setAccessible(true);
            }
            return m;
        }

        @Nullable
        public Field getField(String name) {
            if(fieldCache.containsKey(name)) {
                return fieldCache.get(name);
            }

            try {
                // find the field in the class
                Field f = this.clazz.getDeclaredField(name);
                fieldCache.put(name, f);
                return f;
            } catch(NoSuchFieldException e) {
                // find the field in parent classes
                Class<?> superClass = this.clazz.getSuperclass();
                while(superClass != Object.class) {
                    try {
                        Field f = superClass.getDeclaredField(name);
                        fieldCache.put(name, f);
                        return f;
                    } catch(NoSuchFieldException ignored) {
                        superClass = superClass.getSuperclass();
                    }
                }

                // not found in any parent class :(
                return null;
            }
        }

        @Nullable
        public Field getFieldAccessible(String name) {
            Field f = getField(name);
            if(f == null) {
                return null;
            }
            if(!f.isAccessible()) {
                f.setAccessible(true);
            }
            return f;
        }
    }

}
