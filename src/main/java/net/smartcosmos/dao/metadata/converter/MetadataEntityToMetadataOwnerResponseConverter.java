package net.smartcosmos.dao.metadata.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;
import net.smartcosmos.dao.metadata.util.UuidUtil;
import net.smartcosmos.dto.metadata.MetadataOwnerResponse;

@Component
public class MetadataEntityToMetadataOwnerResponseConverter
    implements Converter<MetadataEntity, MetadataOwnerResponse>, FormatterRegistrar {

    @Override
    public MetadataOwnerResponse convert(MetadataEntity entity) {

        MetadataOwnerEntity owner = entity.getOwner();

        return MetadataOwnerResponse.builder()
            .ownerType(owner.getType())
            .ownerUrn(UuidUtil.getThingUrnFromUuid(owner.getId()))
            .tenantUrn(UuidUtil.getTenantUrnFromUuid(owner.getTenantId()))
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}

