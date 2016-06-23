package net.smartcosmos.dao.metadata.repository;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetadataRepository extends JpaRepository<MetadataEntity, UUID>, JpaSpecificationExecutor<MetadataEntity> {

    Long countByTenantIdAndOwnerTypeIgnoreCaseAndOwnerIdAndKeyNameIgnoreCaseIn(
        UUID tenantId,
        String ownerType,
        UUID ownerId,
        Collection<String> keyNames);

    List<MetadataEntity> findByTenantIdAndOwnerId(UUID tenantId, UUID ownerId);

    Optional<MetadataEntity> findByTenantIdAndOwnerTypeIgnoreCaseAndOwnerIdAndKeyNameIgnoreCase(
        UUID tenantId,
        String ownerType,
        UUID ownerId,
        String keyName);

    List<MetadataEntity> findByTenantIdAndOwnerTypeIgnoreCaseAndOwnerId(
        UUID tenantId,
        String ownerType,
        UUID ownerId);

    @Transactional
    List<MetadataEntity> deleteByTenantIdAndOwnerTypeIgnoreCaseAndOwnerIdAndKeyNameIgnoreCase(
        UUID tenantId,
        String ownerType,
        UUID ownerId,
        String keyName);

    @Transactional
    List<MetadataEntity> deleteByTenantIdAndOwnerTypeIgnoreCaseAndOwnerId(
        UUID tenantId,
        String ownerType,
        UUID ownerId);
}
