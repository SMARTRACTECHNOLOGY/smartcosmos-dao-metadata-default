package net.smartcosmos.dao.metadata.converter;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.util.MetadataValueParser;
import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.util.UuidUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MetadataEntityListToMetadataResponseConverter {

    public MetadataResponse convert(List<MetadataEntity> entities) {

        if (entities == null) {
            return null;
        }
        if (entities.size() == 0) {
            return null;
        }

        Map<String, Object> metadata = new HashMap<>();
        for (MetadataEntity entity : entities) {
            Object o = MetadataValueParser.parseValue(entity);
            metadata.put(entity.getKeyName(), o);
        }

        return MetadataResponse.builder()
            .ownerType(entities.get(0).getOwnerType())
            .ownerUrn(UuidUtil.getUrnFromUuid(entities.get(0).getOwnerId()))
            .metadata(metadata)
            .tenantUrn(UuidUtil.getAccountUrnFromUuid(entities.get(0).getTenantId()))
            .build();
    }
}
