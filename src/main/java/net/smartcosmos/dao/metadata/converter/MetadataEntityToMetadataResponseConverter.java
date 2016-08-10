package net.smartcosmos.dao.metadata.converter;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.util.MetadataValueParser;
import net.smartcosmos.dao.metadata.util.UuidUtil;
import net.smartcosmos.dto.metadata.MetadataResponse;

@Component
public class MetadataEntityToMetadataResponseConverter
    implements Converter<MetadataEntity, MetadataResponse>, FormatterRegistrar {

    @Override
    public MetadataResponse convert(MetadataEntity entity) {

        Map<String, Object> metadata = new HashMap<>();
        Object value = MetadataValueParser.parseValue(entity);
        metadata.put(entity.getKeyName(), value);

        return MetadataResponse.builder()
            .ownerType(entity.getOwner()
                           .getType())
            .ownerUrn(UuidUtil.getThingUrnFromUuid(entity.getOwner()
                                                       .getId()))
            .metadata(metadata)
            .tenantUrn(UuidUtil.getTenantUrnFromUuid(entity.getOwner()
                                                         .getTenantId()))
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {

        registry.addConverter(this);
    }
}
