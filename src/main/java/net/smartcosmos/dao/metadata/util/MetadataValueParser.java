package net.smartcosmos.dao.metadata.util;

import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import org.json.JSONArray;
import org.json.JSONObject;

@Slf4j
public class MetadataValueParser {

    private static final String JSON_DATA_TYPE_STRING = "String";
    private static final String JSON_DATA_TYPE_BOOLEAN = "Boolean";
    private static final String JSON_DATA_TYPE_INTEGER = "Integer";
    private static final String JSON_DATA_TYPE_LONG = "Long";
    private static final String JSON_DATA_TYPE_FLOAT = "Float";
    private static final String JSON_DATA_TYPE_DOUBLE = "Double";
    private static final String JSON_DATA_TYPE_BYTE = "Byte";
    private static final String JSON_DATA_TYPE_SHORT = "Short";
    private static final String JSON_DATA_TYPE_ARRAY = "JSONArray";
    private static final String JSON_DATA_TYPE_OBJECT = "JSONObject";
    private static final String JSON_DATA_TYPE_NULL = "<NULL>";

    /**
     * Check if an object is a number type.
     *
     * @param o Object
     * @return true if o is numeric
     */
    public static boolean isNumber(Object o) {
        return o != null && Number.class.equals(o.getClass().getGenericSuperclass());
    }

    /**
     * Convert the MetadataEntity value into a typed Object depending on dataType
     *
     * @param entity MetadataEntity
     * @return Object of null, Boolean, Number, String or ObjectNode (JSON)
     */
    public static Object parseValue(MetadataEntity entity) {

        if (entity != null && entity.getValue() != null) {

            String value = entity.getValue();

            switch (entity.getDataType()) {

                case JSON_DATA_TYPE_BOOLEAN:
                    return Boolean.parseBoolean(value);
                case JSON_DATA_TYPE_INTEGER:
                    return Integer.valueOf(value);
                case JSON_DATA_TYPE_LONG:
                    return Long.valueOf(value);
                case JSON_DATA_TYPE_FLOAT:
                    return Float.valueOf(value);
                case JSON_DATA_TYPE_DOUBLE:
                    return Double.valueOf(value);
                case JSON_DATA_TYPE_BYTE:
                    return Byte.valueOf(value);
                case JSON_DATA_TYPE_SHORT:
                    return Short.valueOf(value);
                case JSON_DATA_TYPE_ARRAY:
                    return new JSONArray(value);
                case JSON_DATA_TYPE_OBJECT:
                    return new JSONObject(value);
                case JSON_DATA_TYPE_NULL:
                    return JSONObject.NULL;

                case JSON_DATA_TYPE_STRING:
                default:
                    return value;
            }
        }

        return JSONObject.NULL;
    }

    public static String getValue(Object object) {
        if (object != null){
            return object.toString();
        }

        return null;
    }

    public static String getDataType(Object object) {

        if (object == JSONObject.NULL || object == null){
            return JSON_DATA_TYPE_NULL;
        }

        if (object instanceof Boolean) {
            return JSON_DATA_TYPE_BOOLEAN;
        }

        if (object instanceof Integer) {
            return JSON_DATA_TYPE_INTEGER;
        }

        if (object instanceof Float) {
            return JSON_DATA_TYPE_FLOAT;
        }

        if (object instanceof Double) {
            return JSON_DATA_TYPE_DOUBLE;
        }

        if (object instanceof Byte) {
            return JSON_DATA_TYPE_BYTE;
        }

        if (object instanceof Short) {
            return JSON_DATA_TYPE_SHORT;
        }

        if (object instanceof JSONObject) {
            return JSON_DATA_TYPE_OBJECT;
        }

        if (object instanceof JSONArray) {
            return JSON_DATA_TYPE_ARRAY;
        }

        return JSON_DATA_TYPE_STRING;
    }
}
