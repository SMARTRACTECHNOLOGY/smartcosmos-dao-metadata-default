package net.smartcosmos.dao.metadata.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;

import java.io.IOException;

@Slf4j
public class MetadataValueParser {

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
        Object o = null;

        // Null
        if ((entity.getValue() == null) || (entity.getDataType().equalsIgnoreCase("null"))) {
            return null;
        }
        // Boolean
        if (Boolean.class.getSimpleName().equals(entity.getDataType())) {
            o = Boolean.parseBoolean(entity.getValue());
        }
        // Double
        if (Double.class.getSimpleName().equals(entity.getDataType())) {
            o = Double.parseDouble(entity.getValue());
        }
        // Integer
        if (Integer.class.getSimpleName().equals(entity.getDataType())) {
            o = Integer.parseInt(entity.getValue());
        }
        // Long
        if (Long.class.getSimpleName().equals(entity.getDataType())) {
            o = Long.parseLong(entity.getValue());
        }
        // String
        if (String.class.getSimpleName().equals(entity.getDataType())) {
            o = entity.getValue();
        }
        // JSONObject
        if (ObjectNode.class.getSimpleName().equals(entity.getDataType())) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
                o = mapper.readTree(entity.getValue());
            } catch (IOException ex) {
                log.debug(ex.getMessage());
                log.warn("Unparseable JSON object in metadata value, returning string instead!");
                o = entity.getValue();
            }
        }
        // Catch-all: everything else will be returned as String
        if (o == null) {
            o = entity.getValue();
        }
        return o;
    }

}
