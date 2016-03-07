package com.clearcapital.oss.java;

import com.clearcapital.oss.java.exceptions.AssertException;

public class AssertHelpers {

    public static void isFalse(final Boolean expression, final String message) throws AssertException {
        if (!checkIsFalse(expression)) {
            throw new AssertException(message);
        }
    }

    public static void isTrue(final Boolean expression, final String message) throws AssertException {
        if (checkIsFalse(expression)) {
            throw new AssertException(message);
        }
    }

    public static void isNull(final Object object, final String message) throws AssertException {
        if (!checkIsNull(object)) {
            throw new AssertException(message);
        }
    }

    public static void notFalse(final Boolean expression, final String message) throws AssertException {
        if (checkIsFalse(expression)) {
            throw new AssertException(message);
        }
    }

    public static void notNull(final Object object, final String message) throws AssertException {
        if (checkIsNull(object)) {
            throw new AssertException(message);
        }
    }

    private static Boolean checkIsFalse(final Boolean expression) {
        return (expression == null || !expression);
    }

    private static Boolean checkIsNull(final Object object) {
        return (object == null);
    }
}