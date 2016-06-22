package net.smartcosmos.dao.metadata.util;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import org.json.JSONArray;
import org.json.JSONObject;
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
        MetadataEntity entity = MetadataEntity.builder()
            .dataType("<NULL>")
            .build();

        Object o = MetadataValueParser.parseValue(entity);

        assertEquals(JSONObject.NULL, o);
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
            .dataType("JSONObject")
            .value(input.toString())
            .build();

        Object o = MetadataValueParser.parseValue(entity);
        assertTrue(o instanceof JSONObject);
        JSONObject output = (JSONObject) o;

        assertEquals(1, output.get("x"));
        assertEquals(2, output.get("y"));
    }

    @Test
    public void thatJsonArrayCanBeParsed() throws Exception {

        String input = "[{\"x\":1},{\"x\":2}]";
        MetadataEntity entity = MetadataEntity.builder()
            .dataType("JSONArray")
            .value(input.toString())
            .build();

        Object o = MetadataValueParser.parseValue(entity);
        assertTrue(o instanceof JSONArray);
        JSONArray output = (JSONArray) o;

        assertEquals(1, ((JSONArray)output).getJSONObject(0).get("x"));
        assertEquals(2, ((JSONArray)output).getJSONObject(1).get("x"));
    }

}
