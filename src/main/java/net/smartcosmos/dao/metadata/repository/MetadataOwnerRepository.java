package net.smartcosmos.dao.metadata.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;

public interface MetadataOwnerRepository extends JpaRepository<MetadataOwnerEntity, UUID> {

    MetadataOwnerEntity findByTenantIdAndTypeIgnoreCaseAndId(UUID tenantId, String type, UUID id);

    List<MetadataOwnerEntity> findByTenantId(UUID tenantId);

    Page<MetadataOwnerEntity> findByTenantIdAndInternalIdIn(UUID tenantId, Collection<UUID> internalIds, Pageable pageable);

    @Transactional
    List<MetadataOwnerEntity> deleteByTenantIdAndTypeIgnoreCaseAndId(UUID tenantId, String type, UUID id);
}
