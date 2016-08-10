package net.smartcosmos.dao.metadata.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.util.MetadataValueParser;
import net.smartcosmos.dao.metadata.util.UuidUtil;
import net.smartcosmos.dto.metadata.MetadataSingleResponse;

@Component
public class MetadataEntityToMetadataSingleResponseConverter
    implements Converter<MetadataEntity, MetadataSingleResponse>, FormatterRegistrar {

    @Override
    public MetadataSingleResponse convert(MetadataEntity entity) {

        return MetadataSingleResponse.builder()
            .ownerType(entity.getOwner()
                           .getType())
            .ownerUrn(UuidUtil.getThingUrnFromUuid(entity.getOwner()
                                                       .getId()))
            .key(entity.getKeyName())
            .dataType(entity.getDataType()
                          .toString())
            .value(MetadataValueParser.parseValue(entity))
            .tenantUrn(UuidUtil.getTenantUrnFromUuid(entity.getOwner()
                                                         .getTenantId()))
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {

        registry.addConverter(this);
    }
}
