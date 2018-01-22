package com.jarlene.java.utils;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReflectUtil {


    private final Class<?> type;

    private final Object object;

    private static final Constructor<MethodHandles.Lookup> CACHED_LOOKUP_CONSTRUCTOR;

    static {
        try {
            CACHED_LOOKUP_CONSTRUCTOR = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);

            if (!CACHED_LOOKUP_CONSTRUCTOR.isAccessible())
                CACHED_LOOKUP_CONSTRUCTOR.setAccessible(true);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public ReflectUtil create() throws RuntimeException {
        return create(new Object[0]);
    }

    public ReflectUtil create(Object... args) throws RuntimeException {
        Class<?>[] types = types(args);

        try {
            Constructor<?> constructor = type().getDeclaredConstructor(types);
            return on(constructor, args);
        } catch (NoSuchMethodException e) {
            for (Constructor<?> constructor : type().getDeclaredConstructors()) {
                if (match(constructor.getParameterTypes(), types)) {
                    return on(constructor, args);
                }
            }

            throw new RuntimeException(e);
        }
    }

    public static ReflectUtil on(String name) throws RuntimeException {
        return on(forName(name));
    }

    public static ReflectUtil on(String name, ClassLoader classLoader) throws RuntimeException {
        return on(forName(name, classLoader));
    }

    public static ReflectUtil on(Object object) {
        return new ReflectUtil(object == null ? Object.class : object.getClass(), object);
    }

    public static ReflectUtil on(Class<?> clazz) {
        return new ReflectUtil(clazz);
    }

    private static ReflectUtil on(Constructor<?> constructor, Object... args) throws RuntimeException {
        try {
            return on(constructor.getDeclaringClass(), accessible(constructor).newInstance(args));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ReflectUtil on(Method method, Object object, Object... args) throws RuntimeException {
        try {
            accessible(method);

            if (method.getReturnType() == void.class) {
                method.invoke(object, args);
                return on(object);
            } else {
                return on(method.invoke(object, args));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <P> P as(final Class<P> proxyType) {
        final boolean isMap = (object instanceof Map);
        final InvocationHandler handler = new InvocationHandler() {
            @SuppressWarnings("null")
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();

                try {
                    return on(type, object).call(name, args).get();
                } catch (RuntimeException e) {
                    if (isMap) {
                        Map<String, Object> map = (Map<String, Object>) object;
                        int length = (args == null ? 0 : args.length);

                        if (length == 0 && name.startsWith("get")) {
                            return map.get(property(name.substring(3)));
                        } else if (length == 0 && name.startsWith("is")) {
                            return map.get(property(name.substring(2)));
                        } else if (length == 1 && name.startsWith("set")) {
                            map.put(property(name.substring(3)), args[0]);
                            return null;
                        }
                    }

                    if (method.isDefault()) {
                        return CACHED_LOOKUP_CONSTRUCTOR
                                .newInstance(proxyType)
                                .unreflectSpecial(method, proxyType)
                                .bindTo(proxy)
                                .invokeWithArguments(args);
                    }

                    throw e;
                }
            }
        };

        return (P) Proxy.newProxyInstance(proxyType.getClassLoader(), new Class[]{proxyType}, handler);
    }

    public ReflectUtil call(String name) throws RuntimeException {
        return call(name, new Object[0]);
    }

    public ReflectUtil call(String name, Object... args) throws RuntimeException {
        Class<?>[] types = types(args);

        try {
            Method method = exactMethod(name, types);
            return on(method, object, args);
        } catch (NoSuchMethodException e) {
            try {
                Method method = similarMethod(name, types);
                return on(method, object, args);
            } catch (NoSuchMethodException e1) {
                throw new RuntimeException(e1);
            }
        }
    }


    public static <T extends AccessibleObject> T accessible(T accessible) {
        if (accessible == null) {
            return null;
        }

        if (accessible instanceof Member) {
            Member member = (Member) accessible;

            if (Modifier.isPublic(member.getModifiers()) &&
                    Modifier.isPublic(member.getDeclaringClass().getModifiers())) {

                return accessible;
            }
        }
        if (!accessible.isAccessible()) {
            accessible.setAccessible(true);
        }

        return accessible;
    }

    public Map<String, ReflectUtil> fields() {
        Map<String, ReflectUtil> result = new LinkedHashMap<String, ReflectUtil>();
        Class<?> t = type();

        do {
            for (Field field : t.getDeclaredFields()) {
                if (type != object ^ Modifier.isStatic(field.getModifiers())) {
                    String name = field.getName();

                    if (!result.containsKey(name))
                        result.put(name, field(name));
                }
            }

            t = t.getSuperclass();
        }
        while (t != null);

        return result;
    }

    public <T> T get() {
        return (T) object;
    }

    public <T> T get(String name) throws RuntimeException {
        return field(name).get();
    }

    public ReflectUtil set(String name, Object value) throws RuntimeException {
        try {
            Field field = field0(name);
            if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }
            field.set(object, unwrap(value));
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ReflectUtil field(String name) throws RuntimeException {
        try {
            Field field = field0(name);
            return on(field.getType(), field.get(object));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Field field0(String name) throws RuntimeException {
        Class<?> t = type();

        try {
            return accessible(t.getField(name));
        } catch (NoSuchFieldException e) {
            do {
                try {
                    return accessible(t.getDeclaredField(name));
                } catch (NoSuchFieldException ignore) {
                }

                t = t.getSuperclass();
            }
            while (t != null);

            throw new RuntimeException(e);
        }
    }

    public Class<?> type() {
        return type;
    }

    private ReflectUtil(Class<?> type) {
        this(type, type);
    }

    private ReflectUtil(Class<?> type, Object object) {
        this.type = type;
        this.object = object;
    }

    private static ReflectUtil on(Class<?> type, Object object) {
        return new ReflectUtil(type, object);
    }

    private static Class<?> forName(String name) throws RuntimeException {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> forName(String name, ClassLoader classLoader) throws RuntimeException {
        try {
            return Class.forName(name, true, classLoader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object unwrap(Object object) {
        if (object instanceof ReflectUtil) {
            return ((ReflectUtil) object).get();
        }

        return object;
    }

    private static class NULL {
    }

    public static Class<?> wrapper(Class<?> type) {
        if (type == null) {
            return null;
        } else if (type.isPrimitive()) {
            if (boolean.class == type) {
                return Boolean.class;
            } else if (int.class == type) {
                return Integer.class;
            } else if (long.class == type) {
                return Long.class;
            } else if (short.class == type) {
                return Short.class;
            } else if (byte.class == type) {
                return Byte.class;
            } else if (double.class == type) {
                return Double.class;
            } else if (float.class == type) {
                return Float.class;
            } else if (char.class == type) {
                return Character.class;
            } else if (void.class == type) {
                return Void.class;
            }
        }

        return type;
    }

    private static Class<?>[] types(Object... values) {
        if (values == null) {
            return new Class[0];
        }

        Class<?>[] result = new Class[values.length];

        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            result[i] = value == null ? NULL.class : value.getClass();
        }

        return result;
    }

    private boolean match(Class<?>[] declaredTypes, Class<?>[] actualTypes) {
        if (declaredTypes.length == actualTypes.length) {
            for (int i = 0; i < actualTypes.length; i++) {
                if (actualTypes[i] == NULL.class)
                    continue;

                if (wrapper(declaredTypes[i]).isAssignableFrom(wrapper(actualTypes[i])))
                    continue;

                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    private static String property(String string) {
        int length = string.length();

        if (length == 0) {
            return "";
        } else if (length == 1) {
            return string.toLowerCase();
        } else {
            return string.substring(0, 1).toLowerCase() + string.substring(1);
        }
    }

    @Override
    public String toString() {
        return object.toString();
    }

    @Override
    public int hashCode() {
        return object.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ReflectUtil) {
            return object.equals(((ReflectUtil) obj).get());
        }

        return false;
    }

    private boolean isSimilarSignature(Method possiblyMatchingMethod, String desiredMethodName, Class<?>[] desiredParamTypes) {
        return possiblyMatchingMethod.getName().equals(desiredMethodName) && match(possiblyMatchingMethod.getParameterTypes(), desiredParamTypes);
    }

    private Method similarMethod(String name, Class<?>[] types) throws NoSuchMethodException {
        Class<?> t = type();

        for (Method method : t.getMethods()) {
            if (isSimilarSignature(method, name, types)) {
                return method;
            }
        }

        do {
            for (Method method : t.getDeclaredMethods()) {
                if (isSimilarSignature(method, name, types)) {
                    return method;
                }
            }

            t = t.getSuperclass();
        }
        while (t != null);

        throw new NoSuchMethodException("No similar method " + name + " with params " + Arrays.toString(types) + " could be found on type " + type() + ".");
    }

    private Method exactMethod(String name, Class<?>[] types) throws NoSuchMethodException {
        Class<?> t = type();

        try {
            return t.getMethod(name, types);
        } catch (NoSuchMethodException e) {
            do {
                try {
                    return t.getDeclaredMethod(name, types);
                } catch (NoSuchMethodException ignore) {
                }

                t = t.getSuperclass();
            }
            while (t != null);

            throw new NoSuchMethodException();
        }
    }

}
