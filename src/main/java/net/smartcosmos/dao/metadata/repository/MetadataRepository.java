package net.smartcosmos.dao.metadata.repository;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetadataRepository extends JpaRepository<MetadataEntity, UUID>, JpaSpecificationExecutor<MetadataEntity> {

    Long countByTenantIdAndOwnerTypeAndOwnerIdAndKeyNameIn(
        UUID tenantId,
        String ownerType,
        UUID ownerId,
        List<String> keyNames);

    List<MetadataEntity> findByTenantIdAndOwnerId(UUID tenantId, UUID ownerId);

    Optional<MetadataEntity> findByTenantIdAndOwnerTypeAndOwnerIdAndKeyName(
        UUID tenantId,
        String ownerType,
        UUID ownerId,
        String keyName);

    List<MetadataEntity> findByTenantIdAndOwnerTypeAndOwnerId(
        UUID tenantId,
        String ownerType,
        UUID ownerId);

    @Transactional
    List<MetadataEntity> deleteByTenantIdAndOwnerTypeAndOwnerIdAndKeyName(
        UUID tenantId,
        String ownerType,
        UUID ownerId,
        String keyName);

    @Transactional
    List<MetadataEntity> deleteByTenantIdAndOwnerTypeAndOwnerId(
        UUID tenantId,
        String ownerType,
        UUID ownerId);
}
