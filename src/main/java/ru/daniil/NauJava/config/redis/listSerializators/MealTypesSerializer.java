package ru.daniil.NauJava.config.redis.listSerializators;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import ru.daniil.NauJava.entity.MealType;

import java.io.IOException;
import java.util.List;

public class MealTypesSerializer extends GenericJackson2JsonRedisSerializer {

    private final ObjectMapper objectMapper;
    private final TypeReference<List<MealType>> productListType =
            new TypeReference<List<MealType>>() {};

    public MealTypesSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Object deserialize(byte[] source) throws SerializationException {
        if (source == null || source.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(source, productListType);
        } catch (IOException e) {
            return super.deserialize(source);
        }
    }

    @Override
    public byte[] serialize(Object source) throws SerializationException {
        try {
            return objectMapper.writeValueAsBytes(source);
        } catch (IOException e) {
            throw new SerializationException("Ошибка сериализации List<MealType>", e);
        }
    }
}
