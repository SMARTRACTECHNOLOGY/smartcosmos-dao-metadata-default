package net.smartcosmos.dao.metadata.converter;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;
import net.smartcosmos.dao.metadata.util.MetadataValueParser;
import net.smartcosmos.dao.metadata.util.UuidUtil;
import net.smartcosmos.dto.metadata.MetadataResponse;

@Component
public class MetadataEntityCollectionToMetadataResponseConverter
    implements Converter<Collection<MetadataEntity>, MetadataResponse>, FormatterRegistrar {

    public MetadataResponse convert(Collection<MetadataEntity> entities) {

        Map<String, Object> metadata = entities.stream()
            .collect(Collectors.toMap(MetadataEntity::getKeyName, MetadataValueParser::parseValue));

        if (!metadata.isEmpty()) {

            MetadataOwnerEntity owner = entities.iterator().next().getOwner();

            return MetadataResponse.builder()
                .ownerType(owner.getType())
                .ownerUrn(UuidUtil.getThingUrnFromUuid(owner.getId()))
                .metadata(metadata)
                .tenantUrn(UuidUtil.getTenantUrnFromUuid(owner.getTenantId()))
                .build();
        }

        return null;
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
