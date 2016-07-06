package net.smartcosmos.dao.metadata.repository;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface MetadataOwnerRepositoryCustom {

    @Transactional
    void addMetadataEntitiesToOwner(UUID internalId, Collection<MetadataEntity> metadataEntities);

    @Transactional
    Optional<MetadataEntity> updateMetadataEntity(UUID internalId, MetadataEntity metadataEntity);
}
