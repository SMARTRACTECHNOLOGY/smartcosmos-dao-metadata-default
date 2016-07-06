package net.smartcosmos.dao.metadata.repository;

import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetadataOwnerRepository extends JpaRepository<MetadataOwnerEntity, UUID>, MetadataOwnerRepositoryCustom {

    Optional<MetadataOwnerEntity> findByTenantIdAndTypeIgnoreCaseAndId(UUID tenantId, String type, UUID id);

    List<MetadataOwnerEntity> findByTenantId(UUID tenantId);

    @Transactional
    List<MetadataOwnerEntity> deleteByTenantIdAndTypeIgnoreCaseAndId(UUID tenantId, String type, UUID id);
}
