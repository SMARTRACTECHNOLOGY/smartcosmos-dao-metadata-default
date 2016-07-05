package net.smartcosmos.dao.metadata.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;
import net.smartcosmos.dao.metadata.util.UuidUtil;
import net.smartcosmos.dto.metadata.MetadataOwnerResponse;

@Component
public class MetadataOwnerToMetadataOwnerResponseConverter
    implements Converter<MetadataOwnerEntity, MetadataOwnerResponse>, FormatterRegistrar {

    @Override
    public MetadataOwnerResponse convert(MetadataOwnerEntity entity) {

        return MetadataOwnerResponse.builder()
            .ownerType(entity.getType())
            .ownerUrn(UuidUtil.getThingUrnFromUuid(entity.getId()))
            .tenantUrn(UuidUtil.getTenantUrnFromUuid(entity.getTenantId()))
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}

