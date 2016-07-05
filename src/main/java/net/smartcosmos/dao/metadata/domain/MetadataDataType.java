package net.smartcosmos.dao.metadata.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum of supported data types for Metadata. The types match the data types support in JSON.
 */
@AllArgsConstructor
public enum MetadataDataType {

    STRING(1, "String"),
    BOOLEAN(2, "Boolean"),
    INTEGER(3, "Integer"),
    LONG(4, "Long"),
    FLOAT(5, "Float"),
    DOUBLE(6, "Double"),
    BYTE(7, "Byte"),
    SHORT(8, "Short"),
    JSON_ARRAY(9, "JSONArray"),
    JSON_OBJECT(10, "JSONObject"),
    JSON_LITERAL_NULL(11, "<NULL>");

    @Getter
    private final Integer id;
    private final String dataType;

    @Override
    public String toString() {
        return this.dataType;
    }

    /**
     * Gets the enum value corresponding to a given ID.
     *
     * @param id the ID as persisted in the database
     * @return the corresponding {@link MetadataDataType} enum value
     * @throws IllegalArgumentException if the ID is unknown
     */
    public static MetadataDataType fromId(Integer id) throws IllegalArgumentException {
        switch (id) {
            case 1: return STRING;
            case 2: return BOOLEAN;
            case 3: return INTEGER;
            case 4: return LONG;
            case 5: return FLOAT;
            case 6: return DOUBLE;
            case 7: return BYTE;
            case 8: return SHORT;
            case 9: return JSON_ARRAY;
            case 10: return JSON_OBJECT;
            case 11: return  JSON_LITERAL_NULL;
        }

        throw new IllegalArgumentException(String.format("There is no MetadataDataType with ID '%d'", id));
    }
}
