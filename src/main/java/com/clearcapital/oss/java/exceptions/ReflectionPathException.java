package com.clearcapital.oss.java.exceptions;

public class ReflectionPathException extends Exception {

    public ReflectionPathException(String message) {
        super(message);
    }

    public ReflectionPathException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectionPathException(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = 8835352654660479610L;

}
