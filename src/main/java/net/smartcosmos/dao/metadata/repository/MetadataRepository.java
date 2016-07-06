package net.smartcosmos.dao.metadata.repository;

import net.smartcosmos.dao.metadata.domain.MetadataDataType;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetadataRepository extends JpaRepository<MetadataEntity, UUID>, JpaSpecificationExecutor<MetadataEntity>, MetadataRepositoryCustom {

    Long countByOwnerAndKeyNameIgnoreCaseIn(MetadataOwnerEntity owner, Collection<String> keyNames);

    List<MetadataEntity> findByOwnerAndKeyNameIgnoreCaseIn(MetadataOwnerEntity owner, Collection<String> keyNames);

    Page<MetadataEntity> findByOwnerIn(Collection<MetadataOwnerEntity> owners, Pageable pageable);

    Optional<MetadataEntity> findByOwnerAndKeyNameIgnoreCase(MetadataOwnerEntity owner, String keyName);

    List<MetadataEntity> findByOwner(MetadataOwnerEntity owner);

    Page<MetadataEntity> findProjectedByTenantIdAndKeyNameAndDataTypeAndValue(UUID tenantId, String keyName, MetadataDataType dataType,
                                                                                       String value, Pageable pageable);
    @Transactional
    List<MetadataEntity> deleteByOwnerAndKeyNameIgnoreCase(MetadataOwnerEntity owner, String keyName);
}
