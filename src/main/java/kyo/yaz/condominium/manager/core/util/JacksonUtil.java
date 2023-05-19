package kyo.yaz.condominium.manager.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.vertx.core.json.jackson.DatabindCodec;

import java.util.Collection;
import java.util.Map;

public class JacksonUtil {

    public static JavaType javaType(ObjectMapper mapper, Class<? extends Collection<?>> collectionClass, Class<?> clazz) {
        return mapper.getTypeFactory().constructCollectionType(collectionClass, clazz);
    }

    public static JavaType javaType(ObjectReader reader, Class<? extends Collection<?>> collectionClass, Class<?> clazz) {
        return reader.getTypeFactory().constructCollectionType(collectionClass, clazz);
    }

    public static JavaType javaType(ObjectWriter writer, Class<? extends Collection<?>> collectionClass, Class<?> clazz) {
        return writer.getTypeFactory().constructCollectionType(collectionClass, clazz);
    }

    public static JavaType javaType(ObjectMapper mapper, Class<? extends Map<?, ?>> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return mapper.getTypeFactory().constructMapType(mapClass, keyClass, valueClass);
    }

    public static JavaType javaType(ObjectReader reader, Class<? extends Map<?, ?>> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return reader.getTypeFactory().constructMapType(mapClass, keyClass, valueClass);
    }

    public static JavaType javaType(ObjectWriter writer, Class<? extends Map<?, ?>> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return writer.getTypeFactory().constructMapType(mapClass, keyClass, valueClass);
    }

    public static JavaType constructParametricType(ObjectMapper mapper, Class<?> parametrized, Class<?>... parameterClasses) {
        return mapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
    }

    public static ObjectReader readerParametricType(ObjectMapper mapper, Class<?> parametrized, Class<?>... parameterClasses) {
        final var javaType = constructParametricType(mapper, parametrized, parameterClasses);
        return mapper.readerFor(javaType);
    }

    public static <T extends ObjectMapper> T defaultConfig(T mapper) {

        mapper
                .registerModule(new JavaTimeModule())
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new BlackbirdModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
                //.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

        return mapper;
    }

    public static JsonMapper jsonMapper() {
        return mapperBuilder(JsonMapper.builder())
                .build();
    }

    public static <M extends ObjectMapper, B extends MapperBuilder<M, B>> MapperBuilder<M, B> mapperBuilder(MapperBuilder<M, B> builder) {
        return builder
                .addModule(new JavaTimeModule())
                .addModule(new ParameterNamesModule())
                .addModule(new Jdk8Module())
                .addModule(new BlackbirdModule())
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
                //.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    }

    public static void validateJson(String json) throws JsonProcessingException {
        DatabindCodec.mapper().readTree(json);
    }
}
