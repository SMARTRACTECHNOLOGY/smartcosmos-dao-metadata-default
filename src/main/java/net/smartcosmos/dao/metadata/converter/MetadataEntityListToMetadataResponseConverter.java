package net.smartcosmos.dao.metadata.converter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.util.MetadataValueParser;
import net.smartcosmos.dao.metadata.util.UuidUtil;
import net.smartcosmos.dto.metadata.MetadataResponse;

@Component
public class MetadataEntityListToMetadataResponseConverter
    implements Converter<List<MetadataEntity>, MetadataResponse>, FormatterRegistrar {

    public MetadataResponse convert(List<MetadataEntity> entities) {

        Map<String, Object> metadata = entities.stream()
            .collect(Collectors.toMap(MetadataEntity::getKeyName, MetadataValueParser::parseValue));

        if (!metadata.isEmpty()) {

            return MetadataResponse.builder()
                .ownerType(entities.get(0).getOwner().getType())
                .ownerUrn(UuidUtil.getThingUrnFromUuid(entities.get(0).getOwner().getId()))
                .metadata(metadata)
                .tenantUrn(UuidUtil.getTenantUrnFromUuid(entities.get(0).getOwner().getTenantId()))
                .build();
        }

        return null;
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
