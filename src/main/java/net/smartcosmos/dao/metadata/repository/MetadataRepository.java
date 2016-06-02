package net.smartcosmos.dao.metadata.repository;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetadataRepository extends JpaRepository<MetadataEntity, UUID> {

    List<MetadataEntity> findByAccountIdAndReferenceId(UUID accountId, UUID referenceId);

    Optional<MetadataEntity> findByAccountIdAndEntityReferenceTypeAndReferenceIdAndKey(UUID accountId, String entityReferenceType, UUID referenceId, String key);

    @Transactional
    List<MetadataEntity> deleteByAccountIdAndEntityReferenceTypeAndReferenceIdAndKey(UUID accountId, String entityReferenceType, UUID referenceId, String key);
}
