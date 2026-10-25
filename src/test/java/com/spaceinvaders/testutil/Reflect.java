package com.spaceinvaders.testutil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Tiny reflection helpers for reaching the view layer's private state in
 * headless tests (Hud's text nodes, GamePanel's lists of live projectiles).
 */
public final class Reflect {

    private Reflect() {}

    /** Reads an instance field, private or not, by walking up the class chain. */
    public static Object get(Object target, String name) {
        try {
            Field field = field(target.getClass(), name);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("no field " + name + " on " + target.getClass(), e);
        }
    }

    /** Captures a field value into a typed reference, for terse test reads. */
    @SuppressWarnings("unchecked")
    public static <T> T getAs(Object target, String name) {
        return (T) get(target, name);
    }

    /** Invokes a no-arg method, private or not, by walking up the class chain. */
    public static Object invoke(Object target, String name) {
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Method method = c.getDeclaredMethod(name);
                method.setAccessible(true);
                return method.invoke(target);
            } catch (NoSuchMethodException ignored) {
                // keep climbing toward the superclasses
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("could not invoke " + name, e);
            }
        }
        throw new AssertionError("no method " + name + " on " + target.getClass());
    }

    private static Field field(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // keep climbing toward the superclasses
            }
        }
        throw new NoSuchFieldException(name);
    }
}
