package net.smartcosmos.dao.metadata.util;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MetadataValueParserTest {

    @Test
    public void thatIsNumberTrue() {
        assertTrue(MetadataValueParser.isNumber(123.45));
    }

    @Test
    public void thatIsNumberFalse() {
        assertFalse(MetadataValueParser.isNumber("Text"));
    }

    @Test
    public void thatNullIsNotANumber() {
        assertFalse(MetadataValueParser.isNumber(null));
    }

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
    public void thatDoubleCanBeParsed() {

        Double input = 123.45;
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
            .dataType("ObjectNode")
            .value(input.toString())
            .build();

        Object o = MetadataValueParser.parseValue(entity);
        ObjectNode output = (ObjectNode) o;

        assertEquals(1, output.findValue("x").asInt());
        assertEquals(2, output.findValue("y").asInt());
    }

    @Test
    public void thatJsonArrayCanBeParsed() throws Exception {

        String input = "[{\"x\":1},{\"x\":2}]";
        MetadataEntity entity = MetadataEntity.builder()
            .dataType("ObjectNode")
            .value(input.toString())
            .build();

        Object o = MetadataValueParser.parseValue(entity);
        ArrayNode output = (ArrayNode) o;

        assertEquals(1, output.get(0).findValue("x").asInt());
        assertEquals(2, output.get(1).findValue("x").asInt());
    }

}
