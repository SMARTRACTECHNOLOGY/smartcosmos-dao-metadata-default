package net.smartcosmos.dao.metadata.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.metadata.domain.MetadataDataType;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Slf4j
public class MetadataValueParser {

    /**
     * Convert the MetadataEntity value into a typed Object depending on dataType
     *
     * @param entity MetadataEntity
     * @return Object of null, Boolean, Number, String, JSONArray or JSONObject (JSON)
     */
    public static Object parseValue(MetadataEntity entity) {

        if (entity != null && entity.getValue() != null) {

            String value = entity.getValue();

            switch (entity.getDataType()) {

                case BOOLEAN:
                    return Boolean.parseBoolean(value);
                case INTEGER:
                    return Integer.valueOf(value);
                case LONG:
                    return Long.valueOf(value);
                case FLOAT:
                    return Float.valueOf(value);
                case DOUBLE:
                    return Double.valueOf(value);
                case BYTE:
                    return Byte.valueOf(value);
                case SHORT:
                    return Short.valueOf(value);
                case JSON_ARRAY:
                case JSON_OBJECT:
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.enableDefaultTyping();
                    try {
                        return mapper.readTree(entity.getValue());
                    } catch (IOException e) {
                        log.warn("MetadataValueParser.parseValue: Error parsing JSON, returning String instead.");
                    }
                case JSON_LITERAL_NULL:
                    return JSONObject.NULL;

                case STRING:
                default:
                    return value;
            }
        }

        return JSONObject.NULL;
    }

    /**
     * Gets the String representation of an object.
     *
     * @param object the value object
     * @return the object's string representation
     */
    public static String getValue(Object object) {
        if (object != null) {

            if ((MetadataDataType.JSON_OBJECT == getDataType(object)) ||
                (MetadataDataType.JSON_ARRAY == getDataType(object))) {

                ObjectMapper mapper = new ObjectMapper();
                mapper.enableDefaultTyping();
                try {
                    return mapper.writeValueAsString(object);
                } catch (JsonProcessingException e) {
                    log.warn("MetadataValueParser.getValue: Error creating JSON, storing String instead.");
                }
            }
            return object.toString();
        }

        return null;
    }

    /**
     * Gets the database-compatible data type of an Object.
     *
     * @param object the value object
     * @return the data type
     */
    public static MetadataDataType getDataType(Object object) {

        if (object == JSONObject.NULL || object == null){
            return MetadataDataType.JSON_LITERAL_NULL;
        }

        if (object instanceof Boolean) {
            return MetadataDataType.BOOLEAN;
        }

        if (object instanceof Integer) {
            return MetadataDataType.INTEGER;
        }

        if (object instanceof Float) {
            return MetadataDataType.FLOAT;
        }

        if (object instanceof Double) {
            return MetadataDataType.DOUBLE;
        }

        if (object instanceof Byte) {
            return MetadataDataType.BYTE;
        }

        if (object instanceof Short) {
            return MetadataDataType.SHORT;
        }

        if (object instanceof LinkedHashMap) {
            return MetadataDataType.JSON_OBJECT;
        }

        if (object instanceof JSONObject) {
            return MetadataDataType.JSON_OBJECT;
        }

        if (object instanceof ArrayList) {
            return MetadataDataType.JSON_ARRAY;
        }

        if (object instanceof JSONArray) {
            return MetadataDataType.JSON_ARRAY;
        }

        return MetadataDataType.STRING;
    }
}
