package net.smartcosmos.dao.metadata.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;

public interface MetadataOwnerRepository extends JpaRepository<MetadataOwnerEntity, UUID>, MetadataOwnerRepositoryCustom {

    Optional<MetadataOwnerEntity> findByTenantIdAndTypeIgnoreCaseAndId(UUID tenantId, String type, UUID id);

    List<MetadataOwnerEntity> findByTenantId(UUID tenantId);

    Optional<MetadataOwnerEntity> findByTenantIdAndId(UUID tenantId, UUID id);

    @Transactional
    List<MetadataOwnerEntity> deleteByTenantIdAndTypeIgnoreCaseAndId(UUID tenantId, String type, UUID id);
}
