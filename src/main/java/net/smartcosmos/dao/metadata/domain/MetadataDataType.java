package net.smartcosmos.dao.metadata.domain;

import lombok.AllArgsConstructor;

/**
 * Enum of supported data types for Metadata. The types match the data types support in JSON.
 */
@AllArgsConstructor
public enum MetadataDataType {

    STRING("String"),
    BOOLEAN("Boolean"),
    INTEGER("Integer"),
    LONG("Long"),
    FLOAT("Float"),
    DOUBLE("Double"),
    BYTE("Byte"),
    SHORT("Short"),
    JSON_ARRAY("JSONArray"),
    JSON_OBJECT("JSONObject"),
    JSON_LITERAL_NULL("<NULL>");

    private final String dataType;

    @Override
    public String toString() {
        return this.dataType;
    }
}
