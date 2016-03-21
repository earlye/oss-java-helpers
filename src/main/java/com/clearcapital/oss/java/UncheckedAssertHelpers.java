package com.clearcapital.oss.java;

public class UncheckedAssertHelpers {

    public static void isFalse(final Boolean expression, final String message) throws IllegalStateException {
        if (!checkIsFalse(expression)) {
            throw new IllegalStateException(message);
        }
    }

    public static void isTrue(final Boolean expression, final String message) throws IllegalStateException {
        if (checkIsFalse(expression)) {
            throw new IllegalStateException(message);
        }
    }

    public static void isNull(final Object object, final String message) throws IllegalStateException {
        if (!checkIsNull(object)) {
            throw new IllegalStateException(message);
        }
    }

    public static void notFalse(final Boolean expression, final String message) throws IllegalStateException {
        if (checkIsFalse(expression)) {
            throw new IllegalStateException(message);
        }
    }

    public static void notNull(final Object object, final String message) throws IllegalStateException {
        if (checkIsNull(object)) {
            throw new IllegalStateException(message);
        }
    }

    private static Boolean checkIsFalse(final Boolean expression) {
        return (expression == null || !expression);
    }

    private static Boolean checkIsNull(final Object object) {
        return (object == null);
    }
}