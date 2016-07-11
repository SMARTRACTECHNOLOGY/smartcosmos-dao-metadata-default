package net.smartcosmos.dao.metadata.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.validation.ConstraintViolationException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;
import org.springframework.util.Assert;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;

@Component
public class MetadataOwnerRepositoryImpl implements MetadataOwnerRepositoryCustom {

    @Lazy
    private final MetadataOwnerRepository repository;

    @Lazy
    @Autowired
    public MetadataOwnerRepositoryImpl(MetadataOwnerRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addMetadataEntitiesToOwner(UUID internalId, Collection<MetadataEntity> metadataEntities) {

        Assert.notNull(internalId, "ownerId must not be null");

        MetadataOwnerEntity owner = repository.findOne(internalId);
        if (owner == null) {
            throw new IllegalArgumentException(String.format("No MetadataOwnerEntity with internal ID '%s'", internalId));
        }

        Map<String, MetadataEntity> ownerMetadataEntities = initMetadataEntities(owner);
        for (MetadataEntity metadataEntity : metadataEntities) {
            metadataEntity.setOwner(owner);

            ownerMetadataEntities.putIfAbsent(metadataEntity.getKeyName(), metadataEntity);
        }
    }

    @Override
    public Optional<MetadataEntity> updateMetadataEntity(UUID internalId, MetadataEntity metadataEntity) {

        Assert.notNull(internalId, "ownerId must not be null");

        MetadataOwnerEntity owner = repository.findOne(internalId);
        if (owner == null) {
            throw new IllegalArgumentException(String.format("No MetadataOwnerEntity with internal ID '%s'", internalId));
        }

        Map<String, MetadataEntity> map = initMetadataEntities(owner);

        if (map.containsKey(metadataEntity.getKeyName())) {
            metadataEntity.setOwner(owner);
            map.replace(metadataEntity.getKeyName(), metadataEntity);
            persist(owner);

            return Optional.ofNullable(metadataEntity);
        }

        return Optional.empty();
    }

    @Override
    public void orphanDelete(UUID tenantId, String type, UUID id) {

        Assert.notNull(tenantId, "tenantId must not be null");
        Assert.notNull(id, "id must not be null");

        Optional<MetadataOwnerEntity> owner = repository.findByTenantIdAndTypeIgnoreCaseAndId(tenantId, type, id);

        if (owner.isPresent() && owner.get().getMetadataEntities().isEmpty()) {
            repository.delete(owner.get());
        }
    }

    @Override
    public Map<String, MetadataEntity> getAssociatedMetadataEntities(UUID internalId) {

        Assert.notNull(internalId, "ownerId must not be null");

        MetadataOwnerEntity owner = repository.findOne(internalId);
        if (owner != null) {
            Map<String, MetadataEntity> metadataEntities = initMetadataEntities(owner);

            return metadataEntities;
        }

        throw new IllegalArgumentException(String.format("No MetadataOwnerEntity with internal ID '%s'", internalId));
    }

    private Map<String, MetadataEntity> initMetadataEntities(MetadataOwnerEntity owner) {
        Map<String, MetadataEntity> metadataEntities = owner.getMetadataEntities();

        if (!Hibernate.isInitialized(metadataEntities)) {
            Hibernate.initialize(metadataEntities);
        }

        return metadataEntities;
    }

    private MetadataOwnerEntity persist (MetadataOwnerEntity entity) throws ConstraintViolationException, TransactionException {
        try {
            return repository.save(entity);
        } catch (TransactionException e) {
            // we expect constraint violations to be the root cause for exceptions here,
            // so we throw this particular exception back to the caller
            if (ExceptionUtils.getRootCause(e) instanceof ConstraintViolationException) {
                throw (ConstraintViolationException) ExceptionUtils.getRootCause(e);
            } else {
                throw e;
            }
        }
    }
}
