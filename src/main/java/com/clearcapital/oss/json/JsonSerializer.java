package com.clearcapital.oss.json;

import java.io.IOException;

import com.clearcapital.oss.java.Serializer;
import com.clearcapital.oss.java.exceptions.DeserializingException;
import com.clearcapital.oss.java.exceptions.SerializingException;
import com.clearcapital.oss.json.serializers.NullKeyAsEmptySerializer;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class JsonSerializer implements Serializer {

    public final static JsonSerializer INSTANCE = new JsonSerializer();

    final ObjectMapper objectMapper;

    public JsonSerializer() {
        objectMapper = configureObjectMapper(new ObjectMapper());
    }

    static final public ObjectMapper configureObjectMapper(final ObjectMapper mapper) {
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SerializerProvider sp = mapper.getSerializerProvider();
        if (sp == null) {
            DefaultSerializerProvider dp = new DefaultSerializerProvider.Impl();
            mapper.setSerializerProvider(dp);
            sp = dp;
        }
        sp.setNullKeySerializer(new NullKeyAsEmptySerializer());

        SimpleModule globalSerializers = new SimpleModule();
        globalSerializers.addSerializer(Long.class, ToStringSerializer.instance);
        mapper.registerModule(globalSerializers);

        return mapper;
    }

    public static JsonSerializer getInstance() {
        return INSTANCE;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public <T> T getObject(String jsonRepresentation, Class<T> targetType) throws DeserializingException {
        if (jsonRepresentation == null || targetType == null) {
            return null;
        }
        try {
            return targetType.cast(getObjectMapper().readValue(jsonRepresentation, targetType));
        } catch (IOException e) {
            throw new DeserializingException("Could not deserialize json", e);
        }
    }

    @Override
    public <T> T getObject(final String jsonRepresentation, final TypeReference<T> type) throws DeserializingException {
        if (jsonRepresentation == null || type == null) {
            return null;
        }
        try {
            return getObjectMapper().readValue(jsonRepresentation, type);
        } catch (IOException e) {
            throw new DeserializingException("Could not deserialize string.", e);
        }
    }

    @Override
    public String getStringRepresentation(Object objectRepresentation) throws SerializingException {
        try {
            if (objectRepresentation == null) {
                return null;
            }
            return getObjectMapper().writeValueAsString(objectRepresentation);
        } catch (JsonProcessingException e) {
            throw new SerializingException("Could not convert object to Json String", e);
        }
    }

}
