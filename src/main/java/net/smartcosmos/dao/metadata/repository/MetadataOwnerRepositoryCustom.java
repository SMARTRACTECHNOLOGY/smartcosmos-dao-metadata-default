package net.smartcosmos.dao.metadata.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.validation.ConstraintViolationException;

import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Transactional;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;

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

    /**
     * Gets all metadata entities associated to a given owner.
     *
     * @param internalId the owner's internal ID
     * @return the map of metadata entities associated to the owner entity
     */
    @Transactional
    Map<String, MetadataEntity> getAssociatedMetadataEntities(UUID internalId);

    /**
     * Saves a metadata owner entity in an {@link MetadataOwnerRepository}.
     *
     * @param entity the metadata owner entity to persist
     * @return the persisted metadata owner entity
     * @throws ConstraintViolationException if the transaction fails due to violated constraints
     * @throws TransactionException if the transaction fails because of something else
     */
    MetadataOwnerEntity persist (MetadataOwnerEntity entity) throws ConstraintViolationException, TransactionException;
}
