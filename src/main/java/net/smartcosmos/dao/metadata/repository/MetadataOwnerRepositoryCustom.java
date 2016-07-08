package net.smartcosmos.dao.metadata.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;

public interface MetadataOwnerRepositoryCustom {

    /**
     * Updates a metadata owner entity by associating new metadata entities to it.
     *
     * @param internalId the owner's internal ID
     * @param metadataEntities the new metadata entities
     */
    @Transactional
    void addMetadataEntitiesToOwner(UUID internalId, Collection<MetadataEntity> metadataEntities);

    /**
     * Updates a metadata entity already associated to a metadata owner entity.
     *
     * @param internalId the owner's internal ID
     * @param metadataEntity the metadata entity to update
     * @return an Optional with the updated metadata entity or {@code null} if no metadata with the provided entity's key previously existed
     */
    @Transactional
    Optional<MetadataEntity> updateMetadataEntity(UUID internalId, MetadataEntity metadataEntity);

    /**
     * Deletes a metadata owner entity if it is not associated to any metadata entities anymore.
     *
     * @param tenantID the tenant ID
     * @param ownerType the owner type
     * @param ownerId the owner ID
     */
    @Transactional
    void orphanDelete(UUID tenantID, String ownerType, UUID ownerId);
}
