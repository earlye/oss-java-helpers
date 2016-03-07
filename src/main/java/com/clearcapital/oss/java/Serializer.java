package com.clearcapital.oss.java;

import com.clearcapital.oss.java.exceptions.DeserializingException;
import com.clearcapital.oss.java.exceptions.SerializingException;
import com.fasterxml.jackson.core.type.TypeReference;

public interface Serializer {

    public <T> T getObject(String stringRepresentation, Class<T> targetType) throws DeserializingException;

    public <T> T getObject(String stringRepresentation, TypeReference<T> type) throws DeserializingException;

    public String getStringRepresentation(Object objectRepresentation) throws SerializingException;

}
