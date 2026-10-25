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

    /** Writes an instance field, private or not, by walking up the class chain. */
    public static void set(Object target, String name, Object value) {
        try {
            Field field = field(target.getClass(), name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("no writable field " + name + " on " + target.getClass(), e);
        }
    }

    /** Invokes a method with parameters, private or not, by walking up the class chain. */
    public static void invoke(Object target, String name, Class<?>[] types, Object... args) {
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Method method = c.getDeclaredMethod(name, types);
                method.setAccessible(true);
                method.invoke(target, args);
                return;
            } catch (NoSuchMethodException ignored) {
                // keep climbing toward the superclasses
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("could not invoke " + name, e);
            }
        }
        throw new AssertionError("no method " + name + typesSafe(types) + " on " + target.getClass());
    }

    private static String typesSafe(Class<?>[] types) {
        StringBuilder text = new StringBuilder("(");
        for (Class<?> type : types) {
            if (text.length() > 1) text.append(", ");
            text.append(type.getSimpleName());
        }
        return text.append(')').toString();
    }

    /** Invokes a no-arg method, private or not, by walking up the class chain. */
    public static void invoke(Object target, String name) {
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Method method = c.getDeclaredMethod(name);
                method.setAccessible(true);
                method.invoke(target);
                return;
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
