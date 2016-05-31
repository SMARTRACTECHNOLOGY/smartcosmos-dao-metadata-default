package net.smartcosmos.dao.metadata.impl;

import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.metadata.MetadataDao;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.repository.MetadataRepository;
import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.dto.metadata.MetadataUpsert;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MetadataPersistenceService implements MetadataDao {

    private final MetadataRepository metadataRepository;
    private final ConversionService conversionService;

    @Autowired
    public MetadataPersistenceService(MetadataRepository metadataRepository,
                                      ConversionService conversionService) {
        this.metadataRepository = metadataRepository;
        this.conversionService = conversionService;
    }

    @Override
    public List<MetadataResponse> upsert(String accountUrn, Collection<MetadataUpsert> upsertMetadataCollection) throws ConstraintViolationException {

        List<MetadataResponse> responseList = new ArrayList<>();

        for (MetadataUpsert upsertMetadata : upsertMetadataCollection) {
            MetadataEntity entity = conversionService.convert(upsertMetadata, MetadataEntity.class);
            entity = persist(entity);

            responseList.add(conversionService.convert(entity, MetadataResponse.class));
        }

        return responseList;
    }

    @Override
    public Optional<MetadataResponse> delete(String accountUrn, String entityReferenceType, String referenceUrn, String key) {
        return null;
    }

    @Override
    public Optional<MetadataResponse> findByKey(String accountUrn, String entityReferenceType, String referenceUrn, String key) {
        return null;
    }

    /**
     * Saves an metadata entity in an {@link MetadataRepository}.
     *
     * @param metadataEntity the metadata entity to persist
     * @return the persisted metadata entity
     * @throws ConstraintViolationException if the transaction fails due to violated constraints
     * @throws TransactionException if the transaction fails because of something else
     */
    @SuppressWarnings("Duplicates")
    private MetadataEntity persist(MetadataEntity metadataEntity) throws ConstraintViolationException, TransactionException {
        try {
            return metadataRepository.save(metadataEntity);
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
