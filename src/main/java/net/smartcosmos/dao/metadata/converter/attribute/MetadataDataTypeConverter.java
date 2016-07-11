package net.smartcosmos.dao.metadata.converter.attribute;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import net.smartcosmos.dao.metadata.domain.MetadataDataType;

@Converter(autoApply = true)
public class MetadataDataTypeConverter implements AttributeConverter<MetadataDataType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(MetadataDataType attribute) {

        return attribute.getId();
    }

    @Override
    public MetadataDataType convertToEntityAttribute(Integer dbData) {

        return MetadataDataType.fromId(dbData);
    }
}
