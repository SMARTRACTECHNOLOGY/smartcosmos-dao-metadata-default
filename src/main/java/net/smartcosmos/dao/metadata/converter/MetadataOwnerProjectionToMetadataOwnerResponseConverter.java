package net.smartcosmos.dao.metadata.converter;

import net.smartcosmos.dao.metadata.domain.MetadataOwnerProjection;
import net.smartcosmos.dao.metadata.util.UuidUtil;
import net.smartcosmos.dto.metadata.MetadataOwnerResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetadataOwnerProjectionToMetadataOwnerResponseConverter
    implements Converter<MetadataOwnerProjection, MetadataOwnerResponse>, FormatterRegistrar {

    @Override
    public MetadataOwnerResponse convert(MetadataOwnerProjection entity) {

        return MetadataOwnerResponse.builder()
            .ownerType(entity.getOwnerType())
            .ownerUrn(UuidUtil.getThingUrnFromUuid(entity.getOwnerId()))
            .tenantUrn(UuidUtil.getTenantUrnFromUuid(entity.getTenantId()))
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}

