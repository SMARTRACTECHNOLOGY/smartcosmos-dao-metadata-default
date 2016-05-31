package net.smartcosmos.dao.metadata.repository;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface MetadataRepository extends JpaRepository<MetadataEntity, UUID> {

    List<MetadataEntity> findByAccountIdAndReferenceId(UUID accountId, UUID referenceId);

    @Transactional
    List<MetadataEntity> deleteByAccountIdAndEntityReferenceTypeAndReferenceIdAndKey(UUID accountId, String entityReferenceType, UUID referenceId, String key);
}
