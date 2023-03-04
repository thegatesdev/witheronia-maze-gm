package io.github.thegatesdev.witheronia.maze_gm.util.reflection;

import java.lang.reflect.Method;
import java.util.Map;

public class CachedAccessor {

    private final Object object;
    private Map<String, Method> methodCache;

    public CachedAccessor(Object ob) {
        this.object = ob;
    }

    public boolean callMethod(String name, Object... args) {
        final Method method = methodCache.get(name);
        if (method == null) return false;
        try {
            method.invoke(object, args);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean addMethod(String name, Class<?>... parameterTypes) {
        final Method method;
        try {
            method = object.getClass().getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            return false;
        }
        method.setAccessible(true);
        methodCache.put(name, method);
        return true;
    }
}
