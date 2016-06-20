package net.smartcosmos.dao.metadata.converter;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.util.MetadataValueParser;
import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.util.UuidUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MetadataEntityToMetadataResponseConverter
    implements Converter<MetadataEntity, MetadataResponse>, FormatterRegistrar {

    @Override
    public MetadataResponse convert(MetadataEntity entity) {

        Map<String, Object> metadata = new HashMap<>();
        Object o = MetadataValueParser.parseValue(entity);
        metadata.put(entity.getKeyName(), o);

        return MetadataResponse.builder()
            .urn(UuidUtil.getUrnFromUuid(entity.getId()))
            .ownerType(entity.getOwnerType())
            .ownerUrn(UuidUtil.getUrnFromUuid(entity.getOwnerId()))
            .metadata(metadata)
            .tenantUrn(UuidUtil.getAccountUrnFromUuid(entity.getTenantId()))
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
