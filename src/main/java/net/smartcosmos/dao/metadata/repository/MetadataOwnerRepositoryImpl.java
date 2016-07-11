package net.smartcosmos.dao.metadata.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;

@Component
public class MetadataOwnerRepositoryImpl implements MetadataOwnerRepositoryCustom {

    @Lazy
    private final MetadataOwnerRepository repository;

    private final EntityManager entityManager;

    @Lazy
    @Autowired
    public MetadataOwnerRepositoryImpl(MetadataOwnerRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
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

        MetadataOwnerEntity owner = initEntity(internalId);

        if (owner.getMetadataEntities().containsKey(metadataEntity.getKeyName())) {

            metadataEntity.setOwner(owner);

            owner.getMetadataEntities().replace(metadataEntity.getKeyName(), metadataEntity);

            entityManager.merge(owner);
            entityManager.flush();

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

    public MetadataOwnerEntity initEntity(UUID internalId) {
        MetadataOwnerEntity owner = entityManager.find(MetadataOwnerEntity.class, internalId);
        owner = entityManager.merge(owner);

        if (!Hibernate.isInitialized(owner.getMetadataEntities())) {
            Hibernate.initialize(owner.getMetadataEntities());
        }

        return owner;
    }
}
