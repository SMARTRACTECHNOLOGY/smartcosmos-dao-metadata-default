package net.smartcosmos.dao.metadata.util;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.smartcosmos.dao.metadata.domain.MetadataDataType;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetadataValueParserTest {

    @Test
    public void thatNullCanBeParsed() {
        MetadataEntity entity = MetadataEntity.builder()
            .dataType(MetadataDataType.JSON_LITERAL_NULL)
            .build();

        Object o = MetadataValueParser.parseValue(entity);

        assertEquals(NullNode.getInstance(), o);
    }

    @Test
    public void thatBoolCanBeParsed() {

        Boolean input = true;
        MetadataEntity entity = MetadataEntity.builder()
            .dataType(MetadataValueParser.getDataType(input))
            .value(input.toString())
            .build();

        Object o = MetadataValueParser.parseValue(entity);

        assertTrue(input.equals(o));
    }

    @Test
    public void thatDoubleCanBeParsed() {

        Double input = 123.45;
        MetadataEntity entity = MetadataEntity.builder()
            .dataType(MetadataValueParser.getDataType(input))
            .value(input.toString())
            .build();

        Object o = MetadataValueParser.parseValue(entity);

        assertTrue(input.equals(o));
    }

    @Test
    public void thatStringCanBeParsed() {

        String input = "someString";
        MetadataEntity entity = MetadataEntity.builder()
            .dataType(MetadataValueParser.getDataType(input))
            .value(input.toString())
            .build();

        Object o = MetadataValueParser.parseValue(entity);

        assertTrue(input.equals(o));
    }

    @Test
    public void thatIntCanBeParsed() {

        Integer input = 1234567890;
        MetadataEntity entity = MetadataEntity.builder()
            .dataType(MetadataValueParser.getDataType(input))
            .value(input.toString())
            .build();

        Object o = MetadataValueParser.parseValue(entity);

        assertTrue(input.equals(o));
    }

    @Test
    public void thatJsonCanBeParsed() throws Exception {

        String input = "{\"x\":1,\"y\":2}";
        MetadataEntity entity = MetadataEntity.builder()
            .dataType(MetadataDataType.JSON_OBJECT)
            .value(input.toString())
            .build();

        Object o = MetadataValueParser.parseValue(entity);
        assertTrue(o instanceof ObjectNode);
        ObjectNode output = (ObjectNode) o;

        assertEquals(1, output.get("x").asInt());
        assertEquals(2, output.get("y").asInt());
    }

    @Test
    public void thatJsonArrayCanBeParsed() throws Exception {

        String input = "[{\"x\":1},{\"x\":2}]";
        MetadataEntity entity = MetadataEntity.builder()
            .dataType(MetadataDataType.JSON_ARRAY)
            .value(input.toString())
            .build();

        Object o = MetadataValueParser.parseValue(entity);
        assertTrue(o instanceof ArrayNode);
        ArrayNode output = (ArrayNode) o;

        assertEquals(1, output.get(0).get("x").asInt());
        assertEquals(2, output.get(1).get("x").asInt());
    }

}
