package net.smartcosmos.dao.metadata.impl;

import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.metadata.MetadataDao;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.repository.MetadataRepository;
import net.smartcosmos.dto.metadata.MetadataQuery;
import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.dto.metadata.MetadataUpsert;
import net.smartcosmos.util.UuidUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;

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

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

        for (MetadataUpsert upsertMetadata : upsertMetadataCollection) {
            MetadataEntity entity = conversionService.convert(upsertMetadata, MetadataEntity.class);
            entity.setAccountId(accountId);
            entity = persist(entity);

            responseList.add(conversionService.convert(entity, MetadataResponse.class));
        }

        return responseList;
    }

    @Override
    public List<MetadataResponse> delete(String accountUrn, String entityReferenceType, String referenceUrn, String key) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);
        List<MetadataEntity> deleteList = new ArrayList<>();

        try {
            UUID referenceId = UuidUtil.getUuidFromUrn(referenceUrn);
            deleteList = metadataRepository.deleteByAccountIdAndEntityReferenceTypeAndReferenceIdAndKey(
                accountId,
                entityReferenceType,
                referenceId,
                key);
        } catch (IllegalArgumentException e) {
            // empty list will be returned anyway
            log.warn("Illegal URN submitted: %s by account %s", referenceUrn, accountUrn);
        }

        return deleteList.stream()
            .map(o -> conversionService.convert(o, MetadataResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<MetadataResponse> findByKey(String accountUrn, String entityReferenceType, String referenceUrn, String key) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

        Optional<MetadataEntity> entity = Optional.empty();
        try {
            UUID uuid = UuidUtil.getUuidFromUrn(referenceUrn);
            entity = metadataRepository.findByAccountIdAndEntityReferenceTypeAndReferenceIdAndKey(
                accountId,
                entityReferenceType,
                uuid,
                key);
        } catch (IllegalArgumentException e) {
            // empty Optional will be returned anyway
            log.warn("Illegal URN submitted: %s by account %s", referenceUrn, accountUrn);
        }

        if (entity.isPresent())
        {
            final MetadataResponse response = conversionService.convert(entity.get(), MetadataResponse.class);
            return Optional.ofNullable(response);
        }
        return Optional.empty();
    }

    @Override
    public List<MetadataResponse> findBySearchCriterias(String accountUrn, Collection<MetadataQuery> queryMetadataCollection) {
        return null;
    }

    @Override
    public Integer countBySearchCriterias(String accountUrn, Collection<MetadataQuery> queryMetadataCollection) {
        return null;
    }

    /**
     * Saves an metadata entity in an {@link MetadataRepository}.
     *
     * @param entity the metadata entity to persist
     * @return the persisted metadata entity
     * @throws ConstraintViolationException if the transaction fails due to violated constraints
     * @throws TransactionException if the transaction fails because of something else
     */
    @SuppressWarnings("Duplicates")
    private MetadataEntity persist(MetadataEntity entity) throws ConstraintViolationException, TransactionException {
        try {
            return metadataRepository.save(entity);
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
