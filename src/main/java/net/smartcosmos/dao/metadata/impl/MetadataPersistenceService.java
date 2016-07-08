package net.smartcosmos.dao.metadata.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

import net.smartcosmos.dao.metadata.MetadataDao;
import net.smartcosmos.dao.metadata.SortOrder;
import net.smartcosmos.dao.metadata.domain.MetadataDataType;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;
import net.smartcosmos.dao.metadata.repository.MetadataOwnerRepository;
import net.smartcosmos.dao.metadata.repository.MetadataRepository;
import net.smartcosmos.dao.metadata.util.MetadataPersistenceUtil;
import net.smartcosmos.dao.metadata.util.MetadataValueParser;
import net.smartcosmos.dao.metadata.util.SearchSpecifications;
import net.smartcosmos.dao.metadata.util.UuidUtil;
import net.smartcosmos.dto.metadata.MetadataOwnerResponse;
import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.dto.metadata.MetadataSingleResponse;
import net.smartcosmos.dto.metadata.Page;
import net.smartcosmos.dto.metadata.PageInformation;

@Slf4j
@Service
public class MetadataPersistenceService implements MetadataDao {

    private final MetadataRepository metadataRepository;
    private final MetadataOwnerRepository ownerRepository;
    private final ConversionService conversionService;
    private final SearchSpecifications<MetadataEntity> searchSpecifications = new SearchSpecifications<>();

    @Autowired
    public MetadataPersistenceService(MetadataRepository metadataRepository,
                                      MetadataOwnerRepository ownerRepository,
                                      ConversionService conversionService) {
        this.metadataRepository = metadataRepository;
        this.ownerRepository = ownerRepository;
        this.conversionService = conversionService;
    }

    @Override
    public Optional<MetadataResponse> create(String tenantUrn, String ownerType, String ownerUrn, Map<String, Object> metadataMap)
        throws ConstraintViolationException {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        UUID ownerId = UuidUtil.getUuidFromUrn(ownerUrn);

        if (alreadyExists(tenantId, ownerType, ownerId, metadataMap)) {
            return Optional.empty();
        }

        return createOrUpdate(ownerType, metadataMap, tenantId, ownerId);
    }

    private boolean alreadyExists(UUID tenantId, String ownerType, UUID ownerId, Map<String, Object> map) {

        if (tenantId != null && MapUtils.isNotEmpty(map)) {
            Set<String> keys = map.keySet();
            return metadataRepository.countByOwner_TenantIdAndOwner_TypeAndOwner_IdAndKeyNameIgnoreCaseIn(tenantId, ownerType, ownerId, keys) > 0;
        }

        return false;
    }

    @Override
    public Optional<MetadataResponse> upsert(String tenantUrn, String ownerType, String ownerUrn, Map<String, Object> metadataMap)
        throws ConstraintViolationException {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        UUID ownerId = UuidUtil.getUuidFromUrn(ownerUrn);

        return createOrUpdate(ownerType, metadataMap, tenantId, ownerId);
    }

    private MetadataOwnerEntity getOwner(UUID tenantId, String ownerType, UUID ownerId) {

        Optional<MetadataOwnerEntity> owner = ownerRepository.findByTenantIdAndTypeIgnoreCaseAndId(tenantId, ownerType, ownerId);

        if (owner.isPresent()) {
            return owner.get();
        } else {
            MetadataOwnerEntity newOwner = MetadataOwnerEntity.builder()
                .type(ownerType)
                .id(ownerId)
                .tenantId(tenantId)
                .build();

            return persist(newOwner);
        }
    }

    private Optional<MetadataResponse> createOrUpdate(String ownerType, Map<String, Object> metadataMap, UUID tenantId, UUID ownerId) {

        if (MapUtils.isNotEmpty(metadataMap)) {
            Set<String> keys = metadataMap.keySet();

            MetadataOwnerEntity ownerEntity = getOwner(tenantId, ownerType, ownerId);

            List<MetadataEntity> entityList = getMetadataEntities(metadataMap, keys, ownerEntity);
            ownerRepository.addMetadataEntitiesToOwner(ownerEntity.getInternalId(), entityList);

            MetadataResponse response = conversionService.convert(entityList, MetadataResponse.class);

            return Optional.ofNullable(response);
        }

        return Optional.empty();
    }

    private List<MetadataEntity> getMetadataEntities(Map<String, Object> metadataMap, Set<String> keys, MetadataOwnerEntity owner) {
        return keys.stream()
                    .map(key -> {
                        Object object = metadataMap.get(key);
                        String value = MetadataValueParser.getValue(object);
                        MetadataDataType dataType = MetadataValueParser.getDataType(object);

                        return MetadataEntity.builder()
                            .owner(owner)
                            .keyName(key)
                            .value(value)
                            .dataType(dataType)
                            .build();
                    })
                    .collect(Collectors.toList());
    }

    @Override
    public Optional<MetadataResponse> update(
        String tenantUrn,
        String ownerType,
        String ownerUrn,
        String key,
        Object value)
        throws ConstraintViolationException {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        UUID ownerId = UuidUtil.getUuidFromUrn(ownerUrn);

        Optional<MetadataOwnerEntity> owner = ownerRepository.findByTenantIdAndTypeIgnoreCaseAndId(tenantId, ownerType, ownerId);
        if (owner.isPresent()) {

            MetadataDataType dataType = MetadataValueParser.getDataType(value);
            String stringValue = MetadataValueParser.getValue(value);

            MetadataEntity metadataEntity = MetadataEntity.builder()
                .owner(owner.get())
                .keyName(key)
                .dataType(dataType)
                .value(stringValue)
                .build();

            Optional<MetadataEntity> entity = ownerRepository.updateMetadataEntity(owner.get().getInternalId(), metadataEntity);

            if (entity.isPresent()) {
                MetadataResponse response = conversionService.convert(entity.get(), MetadataResponse.class);

                return Optional.ofNullable(response);
            }
        }

        return Optional.empty();
    }

    @Override
    public List<MetadataResponse> delete(String tenantUrn, String ownerType, String ownerUrn, String key) {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        List<MetadataEntity> deleteList = new ArrayList<>();

        try {
            UUID ownerId = UuidUtil.getUuidFromUrn(ownerUrn);
            deleteList = metadataRepository.deleteByOwner_TenantIdAndOwner_TypeAndOwner_IdAndKeyNameIgnoreCase(tenantId, ownerType, ownerId, key);
            ownerRepository.orphanDelete(tenantId, ownerType, ownerId);
        } catch (IllegalArgumentException e) {
            // empty list will be returned anyway
            log.warn("Illegal URN submitted: %s by tenant %s", ownerUrn, tenantUrn);
        }

        return convertList(deleteList, MetadataEntity.class, MetadataResponse.class);
    }

    @Override
    public List<MetadataResponse> deleteAllByOwner(String tenantUrn, String ownerType, String ownerUrn) {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        List<MetadataEntity> deleteList = new ArrayList<>();

        try {
            UUID ownerId = UuidUtil.getUuidFromUrn(ownerUrn);
            List<MetadataOwnerEntity> ownerList = ownerRepository.deleteByTenantIdAndTypeIgnoreCaseAndId(tenantId, ownerType, ownerId);
            if (!ownerList.isEmpty()) {
                deleteList.addAll(ownerList.get(0).getAllMetadataEntities());
            }
        } catch (IllegalArgumentException e) {
            // empty list will be returned anyway
            log.warn("Illegal URN submitted: %s by tenant %s", ownerUrn, tenantUrn);
        }

        return convertList(deleteList, MetadataEntity.class, MetadataResponse.class);
    }

    @Override
    public Optional<Object> findByKey(String tenantUrn, String ownerType, String ownerUrn, String key) {

        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            UUID ownerId = UuidUtil.getUuidFromUrn(ownerUrn);

            Optional<MetadataEntity> entity = metadataRepository.findByOwner_TenantIdAndOwner_TypeAndOwner_IdAndKeyNameIgnoreCase(tenantId, ownerType, ownerId, key);

            if (entity.isPresent()) {
                Object value = MetadataValueParser.parseValue(entity.get());

                return Optional.ofNullable(value);
            }
        } catch (IllegalArgumentException e) {
            // empty Optional will be returned anyway
            log.warn("Illegal URN submitted: %s by account %s", ownerUrn, tenantUrn);
        }

        return Optional.empty();
    }

    @Override
    public Optional<MetadataResponse> findByOwner(
        String tenantUrn,
        String ownerType,
        String ownerUrn,
        Collection<String> keys) {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        UUID ownerId = UuidUtil.getUuidFromUrn(ownerUrn);

        Collection<MetadataEntity> responseCollection;
        if (keys == null || keys.isEmpty()) {
            responseCollection = metadataRepository.findByOwner_TenantIdAndOwner_TypeAndOwner_Id(tenantId, ownerType, ownerId);
        } else {
            responseCollection = metadataRepository.findByOwner_TenantIdAndOwner_TypeAndOwner_IdAndKeyNameIgnoreCaseIn(tenantId, ownerType, ownerId, keys);
        }
        MetadataResponse response = conversionService.convert(responseCollection, MetadataResponse.class);

        return Optional.ofNullable(response);
    }

    @Override
    public Page<MetadataSingleResponse> findAll(String tenantUrn, Integer page, Integer size) {

        return findAllPage(tenantUrn, getPageable(page, size, null, null));
    }

    @Override
    public Page<MetadataSingleResponse> findAll(String tenantUrn, Integer page, Integer size, SortOrder sortOrder, String sortBy) {

        Sort.Direction direction = MetadataPersistenceUtil.getSortDirection(sortOrder);
        sortBy = MetadataPersistenceUtil.getSortByFieldName(sortBy);

        return findAllPage(tenantUrn, getPageable(page, size, sortBy, direction));
    }

    @Override
    public Page<MetadataOwnerResponse> findOwnersByKeyValuePairs(String tenantUrn, Map<String, Object> keyValuePairs, Integer page, Integer size, SortOrder sortOrder, String sortBy) {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);

        Sort.Direction direction = MetadataPersistenceUtil.getSortDirection(sortOrder);
        sortBy = MetadataPersistenceUtil.getSortByFieldName(sortBy, "ownerId");

        if (keyValuePairs.size() == 1) {
            return findOwnerBySingleKeyValuePair(tenantId, keyValuePairs, getPageable(page, size, sortBy, direction));
        } else {
            org.springframework.data.domain.Page<MetadataOwnerEntity> ownerPage = metadataRepository.findProjectedByTenantIdAndKeyValuePairs(tenantId, keyValuePairs, getPageable(page, size, sortBy, direction));
            return convertPage(ownerPage, MetadataOwnerEntity.class, MetadataOwnerResponse.class);
        }
    }

    private Page<MetadataOwnerResponse> findOwnerBySingleKeyValuePair(UUID tenantId, Map<String, Object> keyValuePairs, Pageable pageable) {

        String keyName = keyValuePairs.keySet().iterator().next();
        String value = MetadataValueParser.getValue(keyValuePairs.get(keyName));
        MetadataDataType dataType = MetadataValueParser.getDataType(keyValuePairs.get(keyName));

        org.springframework.data.domain.Page<MetadataEntity> ownerPage = metadataRepository
            .findByOwner_TenantIdAndKeyNameAndDataTypeAndValue(tenantId, keyName, dataType, value, pageable);

        return convertPage(ownerPage, MetadataEntity.class, MetadataOwnerResponse.class);
    }

    private Page<MetadataSingleResponse> findAllPage(String tenantUrn, Pageable pageable) {

        Page<MetadataSingleResponse> result = MetadataPersistenceUtil.emptyPage();
        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            org.springframework.data.domain.Page<MetadataEntity> pageEntity = metadataRepository
                    .findByOwner_TenantId(tenantId, pageable);

            return convertPage(pageEntity, MetadataEntity.class, MetadataSingleResponse.class);
        }
        catch (IllegalArgumentException e) {
            log.warn("Error processing URN: Tenant URN '{}'", tenantUrn);
        }

        return result;
    }

    /**
     * Saves a metadata entity in an {@link MetadataRepository}.
     *
     * @param entity the metadata entity to persist
     * @return the persisted metadata entity
     * @throws ConstraintViolationException if the transaction fails due to violated constraints
     * @throws TransactionException if the transaction fails because of something else
     */
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

    /**
     * Saves a collection of metadata entities in an {@link MetadataRepository}.
     *
     * @param entities the metadata entity to persist
     * @throws ConstraintViolationException if the transaction fails due to violated constraints
     * @throws TransactionException if the transaction fails because of something else
     */
    private void persist(Collection<MetadataEntity> entities)
        throws ConstraintViolationException, TransactionException {

        try {
            metadataRepository.save(entities);
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

    private MetadataOwnerEntity persist(MetadataOwnerEntity entity) throws ConstraintViolationException, TransactionException {
        try {
            return ownerRepository.save(entity);
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

    /**
     * Uses the conversion service to convert a typed list into another typed list.
     *
     * @param list the list
     * @param sourceClass the class of the source type
     * @param targetClass the class of the target type
     * @param <S> the generic source type
     * @param <T> the generic target type
     * @return the converted typed list
     */
    @SuppressWarnings("unchecked")
    private <S, T> List<T> convertList(List<S> list, Class sourceClass, Class targetClass) {

        TypeDescriptor sourceDescriptor = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(sourceClass));
        TypeDescriptor targetDescriptor = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(targetClass));

        return (List<T>) conversionService.convert(list, sourceDescriptor, targetDescriptor);
    }

    /**
     * Uses the conversion service to covert a typed {@link org.springframework.data.domain.Page} into a typed {@link Page}, i.e. converts the page
     * information and the content list.
     *
     * @param page the page
     * @param sourceClass the class of the source type
     * @param targetClass the class of the target type
     * @param <S> the generic source type
     * @param <T> the generic target type
     * @return the converted typed page
     */
    private <S, T> Page<T> convertPage(org.springframework.data.domain.Page<S> page, Class sourceClass, Class targetClass) {

        return Page.<T>builder()
            .page(conversionService.convert(page, PageInformation.class))
            .data(convertList(page.getContent(), sourceClass, targetClass))
            .build();
    }

    /**
     * Builds the pageable for repository calls, including translation of 1-based page numbering on the API level to
     * 0-based page numbering on the repository level.
     *
     * @param page the page number
     * @param size the page size
     * @param sortBy the name of the field to sort by
     * @param direction the sort order direction
     * @return the pageable object
     */
    protected Pageable getPageable(Integer page, Integer size, String sortBy, Sort.Direction direction) {

        if (page < 1) {
            throw new IllegalArgumentException("Page index must not be less than one!");
        }
        page = page - 1;

        if (StringUtils.isBlank(sortBy) || direction == null) {
            return new PageRequest(page, size);
        }

        return new PageRequest(page, size, direction, sortBy);
    }
}
