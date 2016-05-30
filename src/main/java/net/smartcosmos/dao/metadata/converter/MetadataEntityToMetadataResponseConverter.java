package net.smartcosmos.dao.metadata.converter;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.util.UuidUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MetadataEntityToMetadataResponseConverter implements Converter<MetadataEntity, MetadataResponse>, FormatterRegistrar {

    @Override
    public MetadataResponse convert(MetadataEntity entity) {

        return MetadataResponse.builder()
            .urn(UuidUtil.getUrnFromUuid(entity.getId()))
            .entityReferenceType(entity.getEntityReferenceType())
            .referenceUrn(UuidUtil.getUrnFromUuid(entity.getReferenceId()))
            .key(entity.getKey())
            .dataType(entity.getDataType())
            .rawValue(entity.getRawValue())
            .moniker(entity.getMoniker())
            .lastModifiedTimestamp(entity.getLastModified())
            .build();
    }

    public List convertAll(Iterable<MetadataEntity> entities) {

        List<MetadataResponse> convertedList = new ArrayList<>();
        for (MetadataEntity entity : entities) {
            convertedList.add(convert(entity));
        }

        return convertedList;
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
