package net.smartcosmos.dao.metadata.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;

public interface MetadataOwnerRepository extends JpaRepository<MetadataOwnerEntity, UUID> {

    MetadataOwnerEntity findByTenantIdAndTypeIgnoreCaseAndId(UUID tenantId, String type, UUID id);

    List<MetadataOwnerEntity> findByTenantId(UUID tenantId);
}
