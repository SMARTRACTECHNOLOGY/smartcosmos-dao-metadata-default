package net.smartcosmos.dao.metadata.converter;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dto.metadata.MetadataCreate;
import net.smartcosmos.util.UuidUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MetadataCreateToMetadataEntityArrayConverter
        implements Converter<MetadataCreate, MetadataEntity[]>, FormatterRegistrar {

    @Override
    public MetadataEntity[] convert(MetadataCreate entity) {

        MetadataEntity[] metadataEntities = new MetadataEntity[entity.getMetadata().size()];

        int i = 0;
        for (Map.Entry<String, Object> entry : entity.getMetadata().entrySet()) {

            MetadataEntity newEntity = MetadataEntity.builder()
                .ownerType(entity.getOwnerType())
                .ownerId(UuidUtil.getUuidFromUrn(entity.getOwnerUrn()))
                .dataType(entry.getValue().getClass().getSimpleName())
                .keyName(entry.getKey())
                .value(entry.getValue().toString())
                .build();

            metadataEntities[i++] = newEntity;
        }

        return metadataEntities;
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
