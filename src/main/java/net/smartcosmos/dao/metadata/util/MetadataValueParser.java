package net.smartcosmos.dao.metadata.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;

import java.io.IOException;

@Slf4j
public class MetadataValueParser {

    public static Object parseValue(MetadataEntity entity) {
        Object o = null;

        // Null
        if (entity.getValue() == null) {
            return null;
        }
        // Boolean
        if (Boolean.class.getSimpleName().equals(entity.getDataType())) {
            o = Boolean.parseBoolean(entity.getValue());
        }
        // Double
        if (Double.class.getSimpleName().equals(entity.getDataType())) {
            o = Boolean.parseBoolean(entity.getValue());
        }
        // Float
        if (Float.class.getSimpleName().equals(entity.getDataType())) {
            o = Boolean.parseBoolean(entity.getValue());
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
        if (JsonNode.class.getSimpleName().equals(entity.getDataType())) {
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
