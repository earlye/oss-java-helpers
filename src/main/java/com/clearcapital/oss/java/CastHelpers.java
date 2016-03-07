package com.clearcapital.oss.java;
public class CastHelpers {

    /**
     * Cast {@code object} to type specified by {@code resultType}. If {@code object} is not an instance of
     * {@code resultType}, throw a ClassCastException, even if the reason it is not an instance of {@code resultType}
     * is that it is null.
     * 
     * @param object
     * @param resultType
     * @return
     * @throws ClassCastException
     */
    public static <T> T requireCast(final Object object, final Class<T> resultType) throws ClassCastException {
        if (object == null) {
            throw new ClassCastException("Null object is not an instance of required type");
        }
        return resultType.cast(object);
    }

}
