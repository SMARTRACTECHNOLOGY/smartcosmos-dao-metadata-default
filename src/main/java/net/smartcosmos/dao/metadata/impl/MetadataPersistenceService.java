package net.smartcosmos.dao.metadata.impl;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.metadata.MetadataDao;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.repository.MetadataRepository;
import net.smartcosmos.dao.metadata.util.SearchSpecifications;
import net.smartcosmos.dto.metadata.MetadataQuery;
import net.smartcosmos.dto.metadata.MetadataQueryMatchResponse;
import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.dto.metadata.MetadataUpsert;
import net.smartcosmos.util.UuidUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specifications.where;

@Slf4j
@Service
public class MetadataPersistenceService implements MetadataDao {

    public static final int NO_LIMIT_ON_QUERY_RESPONSE_SIZE = 0;

    private final MetadataRepository metadataRepository;
    private final ConversionService conversionService;
    private final SearchSpecifications<MetadataEntity> searchSpecifications = new SearchSpecifications<>();

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
            UUID existingEntityId = getExistingEntityId(accountId, upsertMetadata);

            MetadataEntity entity = conversionService.convert(upsertMetadata, MetadataEntity.class);
            entity.setAccountId(accountId);
            entity.setId(existingEntityId);
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
    public Set<MetadataQueryMatchResponse> findBySearchCriteria(String accountUrn,
                                                                String entityReferenceType,
                                                                Collection<MetadataQuery> queryMetadataCollection,
                                                                Integer queryLimit) {

        Set<MetadataQueryMatchResponse> allMatchResponses = new HashSet<>();
        boolean firstPass = true;

        for (MetadataQuery query: queryMetadataCollection) {
            Specifications<MetadataEntity> specifications = getResponsesForQuery(accountUrn, entityReferenceType, query);
            Collection<MetadataEntity> returnedMetadataEntities = metadataRepository.findAll(specifications);
            Set<MetadataQueryMatchResponse> convertedList = returnedMetadataEntities.stream().map(o -> conversionService.convert(o,
                MetadataQueryMatchResponse.class))

                .collect(Collectors.toSet());
            // First pass? We take it all
            if (firstPass) {
                allMatchResponses.addAll(convertedList);
                firstPass = false;
            }
            // Subsequent passes? We intersect against whatever we already have.
            else {
                Set<MetadataQueryMatchResponse> returnedValuesAsSet = new HashSet<>(convertedList);
                allMatchResponses = Sets.intersection(returnedValuesAsSet, allMatchResponses);

            }
        }
        allMatchResponses.remove(null);
        // limit size ot returns to specified queryLimit
        if (queryLimit != NO_LIMIT_ON_QUERY_RESPONSE_SIZE)
        {
            return allMatchResponses.stream().limit(queryLimit).collect(Collectors.toSet());
        }
        return allMatchResponses;
    }

    @Override
    public Long countBySearchCriteria(String accountUrn, String entityReferenceType, Collection<MetadataQuery> queryMetadataCollection) {

        Set<MetadataQueryMatchResponse> matches = findBySearchCriteria(accountUrn,
            entityReferenceType,
            queryMetadataCollection,
            NO_LIMIT_ON_QUERY_RESPONSE_SIZE);

        return new Long(matches.size());
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

    private UUID getExistingEntityId(UUID accountId, MetadataUpsert upsertMetadata) {
        Optional<MetadataEntity> existingEntity = metadataRepository.findByAccountIdAndEntityReferenceTypeAndReferenceIdAndKey(
            accountId,
            upsertMetadata.getEntityReferenceType(),
            UuidUtil.getUuidFromUrn(upsertMetadata.getReferenceUrn()),
            upsertMetadata.getKey());

        return (existingEntity.isPresent() ? existingEntity.get().getId() : null);
    }

    private Specifications<MetadataEntity> getResponsesForQuery(String accountUrn, String entityReferenceType, MetadataQuery query) {

        Specification<MetadataEntity> accountUrnSpecification = null;

        if (StringUtils.isNotBlank(accountUrn)) {
            UUID accountUuid = UuidUtil.getUuidFromAccountUrn(accountUrn);
            accountUrnSpecification = searchSpecifications.matchUuid(accountUuid, "accountId");
        }

        Specifications<MetadataEntity> specifications = where(accountUrnSpecification);

        Specification<MetadataEntity> keySpecification = getSearchSpecification(MetadataEntity.KEY_FIELD_NAME, query.getKey());
        specifications = specifications.and(keySpecification);

        Specification<MetadataEntity> dataTypeSpecification = getSearchSpecification(MetadataEntity.DATA_TYPE_FIELD_NAME, query.getDataType());
        specifications = specifications.and(dataTypeSpecification);

        Specification<MetadataEntity> rawValueSpecification = getSearchSpecification(MetadataEntity.RAW_VALUE_FIELD_NAME, query.getRawValue());
        specifications = specifications.and(rawValueSpecification);

        Specification<MetadataEntity> entityReferenceTypeSpecification = getSearchSpecification(MetadataEntity.ENTITY_REFERENCE_TYPE_FIELD_NAME,
            entityReferenceType);
        specifications = specifications.and(entityReferenceTypeSpecification);

        return specifications;
    }

    private Specification<MetadataEntity> getSearchSpecification(String fieldName, String query) {
        Specification<MetadataEntity> specification = null;

        if (StringUtils.isNotBlank(fieldName) && StringUtils.isNotBlank(query)) {
            specification = searchSpecifications.stringMatchesExactly(query, fieldName);
        }

        return specification;
    }
}
