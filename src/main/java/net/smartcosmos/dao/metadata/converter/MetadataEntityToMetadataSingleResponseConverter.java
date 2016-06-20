package net.smartcosmos.dao.metadata.converter;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.util.MetadataValueParser;
import net.smartcosmos.dto.metadata.MetadataSingleResponse;
import net.smartcosmos.util.UuidUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetadataEntityToMetadataSingleResponseConverter
        implements Converter<MetadataEntity, MetadataSingleResponse>, FormatterRegistrar {

    @Override
    public MetadataSingleResponse convert(MetadataEntity entity) {

        return MetadataSingleResponse.builder()
            .ownerType(entity.getOwnerType())
            .ownerUrn(UuidUtil.getUrnFromUuid(entity.getOwnerId()))
            .key(entity.getKeyName())
            .dataType(entity.getDataType())
            .value(MetadataValueParser.parseValue(entity))
            .tenantUrn(UuidUtil.getAccountUrnFromUuid(entity.getTenantId()))
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
