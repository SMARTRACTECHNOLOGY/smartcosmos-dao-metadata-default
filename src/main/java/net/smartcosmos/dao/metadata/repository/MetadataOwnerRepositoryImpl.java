package net.smartcosmos.dao.metadata.repository;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Component
public class MetadataOwnerRepositoryImpl implements MetadataOwnerRepositoryCustom {

    private final EntityManager entityManager;

    @Autowired
    public MetadataOwnerRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void addMetadataEntitiesToOwner(UUID internalId, Collection<MetadataEntity> metadataEntities) {

        Assert.notNull(internalId, "internalId must not be null");

        MetadataOwnerEntity owner = entityManager.find(MetadataOwnerEntity.class, internalId);
        owner = entityManager.merge(owner);

        if (!Hibernate.isInitialized(owner.getMetadataEntities())) {
            Hibernate.initialize(owner.getMetadataEntities());
        }

        for (MetadataEntity metadataEntity : metadataEntities) {
            metadataEntity.setOwner(owner);

            owner.getMetadataEntities().putIfAbsent(metadataEntity.getKeyName(), metadataEntity);

        }

        entityManager.merge(owner);
        entityManager.flush();
    }

    @Override
    public Optional<MetadataEntity> updateMetadataEntity(UUID internalId, MetadataEntity metadataEntity) {

        Assert.notNull(internalId, "internalId must not be null");

        MetadataOwnerEntity owner = entityManager.find(MetadataOwnerEntity.class, internalId);
        owner = entityManager.merge(owner);

        if (!Hibernate.isInitialized(owner.getMetadataEntities())) {
            Hibernate.initialize(owner.getMetadataEntities());
        }

        if (owner.getMetadataEntities().containsKey(metadataEntity.getKeyName())) {

            metadataEntity.setOwner(owner);

            owner.getMetadataEntities().replace(metadataEntity.getKeyName(), metadataEntity);

            entityManager.merge(owner);
            entityManager.flush();

            return Optional.ofNullable(metadataEntity);
        }

        return Optional.empty();
    }
}
