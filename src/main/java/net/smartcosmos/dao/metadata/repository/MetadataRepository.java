package net.smartcosmos.dao.metadata.repository;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetadataRepository extends JpaRepository<MetadataEntity, UUID>, JpaSpecificationExecutor<MetadataEntity> {

    List<MetadataEntity> findByTenantIdAndOwnerId(UUID tenantId, UUID ownerId);

    Optional<MetadataEntity> findByTenantIdAndOwnerTypeAndOwnerIdAndKey(
        UUID tenantId,
        String entityReferenceType,
        UUID referenceId,
        String key);

    List<MetadataEntity> findByTenantIdAndOwnerTypeAndOwnerId(
        UUID tenantId,
        String entityReferenceType,
        UUID referenceId);

    @Transactional
    List<MetadataEntity> deleteByTenantIdAndOwnerTypeAndOwnerIdAndKey(
        UUID tenantId,
        String entityReferenceType,
        UUID referenceId,
        String key);

    @Transactional
    List<MetadataEntity> deleteByTenantIdAndOwnerTypeAndOwnerId(
        UUID tenantId,
        String entityReferenceType,
        UUID referenceId);
}
