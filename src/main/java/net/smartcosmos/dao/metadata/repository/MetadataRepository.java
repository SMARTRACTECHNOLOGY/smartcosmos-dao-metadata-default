package net.smartcosmos.dao.metadata.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.smartcosmos.dao.metadata.domain.MetadataDataType;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;

public interface MetadataRepository extends JpaRepository<MetadataEntity, UUID>, JpaSpecificationExecutor<MetadataEntity>, MetadataRepositoryCustom {

    Long countByOwner_TenantIdAndOwner_TypeAndOwner_IdAndKeyNameIn(
        UUID tenantId,
        String ownerType,
        UUID ownerId,
        Collection<String> keyNames);

    List<MetadataEntity> findByOwner_TenantIdAndOwner_TypeAndOwner_IdAndKeyNameIn(
        UUID tenantId,
        String ownerType,
        UUID ownerId,
        Collection<String> keyNames);

    Optional<MetadataEntity> findByOwner_TenantIdAndOwner_TypeAndOwner_IdAndKeyName(
        UUID tenantId,
        String ownerType,
        UUID ownerId,
        String keyName);

    Optional<MetadataEntity> findByOwner_TypeAndOwner_IdAndKeyName(
        String ownerType,
        UUID ownerId,
        String keyName);

    Page<MetadataEntity> findByOwner_TenantIdAndOwner_Type(UUID ownerId, String ownerType, Pageable pageable);

    List<MetadataEntity> findByOwner_TenantIdAndOwner_TypeAndOwner_Id(UUID tenantId, String ownerType, UUID ownerId);

    Page<MetadataEntity> findByOwner_TenantIdAndOwner_TypeAndKeyNameAndDataTypeAndValue(
        UUID tenantId, String ownerType, String keyName,
        MetadataDataType dataType,
        String value, Pageable pageable);

    Page<MetadataEntity> findByOwnerTypeAndKeyNameAndDataTypeAndValue(
        String ownerType, String keyName,
        MetadataDataType dataType,
        String value, Pageable pageable);

    @Transactional
    List<MetadataEntity> deleteByOwner_TenantIdAndOwner_TypeAndOwner_IdAndKeyName(
        UUID tenantId,
        String ownerType,
        UUID ownerId,
        String keyName);
}
