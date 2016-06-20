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
public class MetadataEntityArrayToMetadataResponseConverter
        implements Converter<MetadataEntity[], MetadataResponse>, FormatterRegistrar {

    @Override
    public MetadataResponse convert(MetadataEntity[] entities) {

        if (entities == null) {
            return null;
        }
        if (entities.length == 0) {
            return null;
        }

        Map<String, Object> metadata = new HashMap<>();
        for (MetadataEntity entity : entities) {
            Object o = MetadataValueParser.parseValue(entity);
            metadata.put(entity.getKeyName(), o);
        }

        return MetadataResponse.builder()
            .ownerType(entities[0].getOwnerType())
            .ownerUrn(UuidUtil.getUrnFromUuid(entities[0].getOwnerId()))
            .metadata(metadata)
            .tenantUrn(UuidUtil.getAccountUrnFromUuid(entities[0].getTenantId()))
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
