package net.smartcosmos.dao.metadata.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetadataValueParserTest {

    @Test
    public void thatNullCanBeParsed() {
        MetadataEntity entity = MetadataEntity.builder().build();

        Object o = MetadataValueParser.parseValue(entity);

        assertEquals(null, o);
    }

    @Test
    public void thatBoolCanBeParsed() {

        Boolean input = true;
        MetadataEntity entity = MetadataEntity.builder()
            .dataType(input.getClass().getSimpleName())
            .value(input.toString())
            .build();

        Object o = MetadataValueParser.parseValue(entity);

        assertTrue(input.equals(o));
    }

    @Test
    public void thatStringCanBeParsed() {

        String input = "someString";
        MetadataEntity entity = MetadataEntity.builder()
            .dataType(input.getClass().getSimpleName())
            .value(input.toString())
            .build();

        Object o = MetadataValueParser.parseValue(entity);

        assertTrue(input.equals(o));
    }

    @Test
    public void thatIntCanBeParsed() {

        Integer input = 1234567890;
        MetadataEntity entity = MetadataEntity.builder()
            .dataType(input.getClass().getSimpleName())
            .value(input.toString())
            .build();

        Object o = MetadataValueParser.parseValue(entity);

        assertTrue(input.equals(o));
    }

    @Test
    public void thatJsonCanBeParsed() throws Exception {

        String input = "{\"x\":1,\"y\":2}";
        MetadataEntity entity = MetadataEntity.builder()
            .dataType("JsonNode")
            .value(input.toString())
            .build();

        Object o = MetadataValueParser.parseValue(entity);
        ObjectNode output = (ObjectNode) o;

        assertEquals(1, output.findValue("x").asInt());
        assertEquals(2, output.findValue("y").asInt());
    }

}
