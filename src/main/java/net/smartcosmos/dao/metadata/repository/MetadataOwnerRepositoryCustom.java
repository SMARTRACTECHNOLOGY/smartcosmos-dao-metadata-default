package net.smartcosmos.dao.metadata.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;

public interface MetadataOwnerRepositoryCustom {

    @Transactional
    void addMetadataEntitiesToOwner(UUID internalId, Collection<MetadataEntity> metadataEntities);

    @Transactional
    Optional<MetadataEntity> updateMetadataEntity(UUID internalId, MetadataEntity metadataEntity);

    @Transactional
    void orphanDelete(UUID tenantID, UUID ownerId);
}
