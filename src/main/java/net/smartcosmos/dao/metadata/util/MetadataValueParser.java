package net.smartcosmos.dao.metadata.util;

import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.metadata.domain.MetadataDataType;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import org.json.JSONArray;
import org.json.JSONObject;

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
                    return new JSONArray(value);
                case JSON_OBJECT:
                    return new JSONObject(value);
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
        if (object != null){
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

        if (object instanceof JSONObject) {
            return MetadataDataType.JSON_OBJECT;
        }

        if (object instanceof JSONArray) {
            return MetadataDataType.JSON_ARRAY;
        }

        return MetadataDataType.STRING;
    }
}
