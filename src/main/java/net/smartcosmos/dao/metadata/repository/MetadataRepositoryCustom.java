package net.smartcosmos.dao.metadata.repository;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;

public interface MetadataRepositoryCustom {

    Page<MetadataOwnerEntity> findProjectedByTenantIdAndOwnerTypeAndKeyValuePairs(
        UUID tenantId,
        String ownerType,
        Map<String, Object> keyValuePairs,
        Pageable pageable);
}
