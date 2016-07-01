package net.smartcosmos.dao.metadata.repository;

import net.smartcosmos.dao.metadata.domain.MetadataOwner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface MetadataRepositoryCustom {

    Page<MetadataOwner> findProjectedByTenantIdAndKeyValuePairs(UUID tenantId, Map<String, Object> keyValuePairs, Pageable pageable);
}
