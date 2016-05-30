package net.smartcosmos.dao.metadata.converter;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dto.metadata.MetadataUpsert;
import net.smartcosmos.security.user.SmartCosmosUser;
import net.smartcosmos.security.user.SmartCosmosUserHolder;
import net.smartcosmos.util.UuidUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MetadataUpsertToMetadataEntityConverter implements Converter<MetadataUpsert, MetadataEntity>, FormatterRegistrar {
    @Override
    public MetadataEntity convert(MetadataUpsert entity) {

        // Retrieve current user.
        SmartCosmosUser user = SmartCosmosUserHolder.getCurrentUser();

        return MetadataEntity.builder()
            .accountId(UuidUtil.getUuidFromAccountUrn(user.getAccountUrn()))
            .entityReferenceType(entity.getEntityReferenceType())
            .referenceId(UuidUtil.getUuidFromUrn(entity.getReferenceUrn()))
            .dataType(entity.getDataType())
            .key(entity.getKey())
            .rawValue(entity.getRawValue())
            .moniker(entity.getMoniker())
            .build();
    }

    public List convertAll(Iterable<MetadataUpsert> entities) {

        List<MetadataEntity> convertedList = new ArrayList<>();
        for (MetadataUpsert entity : entities) {
            convertedList.add(convert(entity));
        }

        return convertedList;
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
