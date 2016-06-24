package net.smartcosmos.dao.metadata.impl;

import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.metadata.MetadataDao;
import net.smartcosmos.dao.metadata.domain.MetadataDataType;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.repository.MetadataRepository;
import net.smartcosmos.dao.metadata.util.MetadataPersistenceUtil;
import net.smartcosmos.dao.metadata.util.MetadataValueParser;
import net.smartcosmos.dao.metadata.util.UuidUtil;
import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.dto.metadata.MetadataSingleResponse;
import net.smartcosmos.dto.metadata.Page;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        if (MapUtils.isNotEmpty(map)) {
            Set<String> keys = map.keySet();

            Long count = metadataRepository.countByTenantIdAndOwnerTypeIgnoreCaseAndOwnerIdAndKeyNameIgnoreCaseIn(
                tenantId,
                ownerType,
                ownerId,
                keys);

            return count > 0;
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

    private Optional<MetadataResponse> createOrUpdate(String ownerType, Map<String, Object> metadataMap, UUID tenantId, UUID ownerId) {

        if (MapUtils.isNotEmpty(metadataMap)) {
            Set<String> keys = metadataMap.keySet();


            List<MetadataEntity> entityList = keys.stream()
                .map(key -> {
                    Object object = metadataMap.get(key);
                    String value = MetadataValueParser.getValue(object);
                    MetadataDataType dataType = MetadataValueParser.getDataType(object);

                    return MetadataEntity.builder()
                        .ownerType(ownerType)
                        .ownerId(ownerId)
                        .keyName(key)
                        .value(value)
                        .dataType(dataType)
                        .tenantId(tenantId)
                        .build();
                })
                .collect(Collectors.toList());

            persist(entityList);

            MetadataResponse response = conversionService.convert(entityList, MetadataResponse.class);

            return Optional.ofNullable(response);
        }

        return Optional.empty();
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

        Optional<MetadataEntity> entity = metadataRepository.findByTenantIdAndOwnerTypeIgnoreCaseAndOwnerIdAndKeyNameIgnoreCase(
            tenantId,
            ownerType,
            ownerId,
            key);

        if (entity.isPresent()) {
            MetadataEntity updateEntity = entity.get();

            MetadataDataType dataType = MetadataValueParser.getDataType(value);
            String stringValue = MetadataValueParser.getValue(value);

            updateEntity.setDataType(dataType);
            updateEntity.setValue(stringValue);

            updateEntity = persist(updateEntity);

            MetadataResponse response = conversionService.convert(updateEntity, MetadataResponse.class);

            return Optional.ofNullable(response);
        }

        return Optional.empty();
    }

    @Override
    public List<MetadataResponse> delete(String tenantUrn, String ownerType, String ownerUrn, String key) {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        List<MetadataEntity> deleteList = new ArrayList<>();

        try {
            UUID ownerId = UuidUtil.getUuidFromUrn(ownerUrn);
            deleteList = metadataRepository.deleteByTenantIdAndOwnerTypeIgnoreCaseAndOwnerIdAndKeyNameIgnoreCase(
                tenantId,
                ownerType,
                ownerId,
                key);
        } catch (IllegalArgumentException e) {
            // empty list will be returned anyway
            log.warn("Illegal URN submitted: %s by tenant %s", ownerUrn, tenantUrn);
        }

        return convert(deleteList);
    }

    @Override
    public List<MetadataResponse> deleteAllByOwner(String tenantUrn, String ownerType, String ownerUrn) {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        List<MetadataEntity> deleteList = new ArrayList<>();

        try {
            UUID ownerId = UuidUtil.getUuidFromUrn(ownerUrn);
            deleteList = metadataRepository.deleteByTenantIdAndOwnerTypeIgnoreCaseAndOwnerId(
                tenantId,
                ownerType,
                ownerId);
        } catch (IllegalArgumentException e) {
            // empty list will be returned anyway
            log.warn("Illegal URN submitted: %s by tenant %s", ownerUrn, tenantUrn);
        }

        return convert(deleteList);
    }

    @Override
    public Optional<Object> findByKey(String tenantUrn, String ownerType, String ownerUrn, String key) {

        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            UUID ownerId = UuidUtil.getUuidFromUrn(ownerUrn);

            Optional<MetadataEntity> entity = metadataRepository.findByTenantIdAndOwnerTypeIgnoreCaseAndOwnerIdAndKeyNameIgnoreCase(
                tenantId,
                ownerType,
                ownerId,
                key);

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

        List<MetadataEntity> responseList;
        if (keys == null || keys.isEmpty()) {
            responseList = metadataRepository.findByTenantIdAndOwnerTypeIgnoreCaseAndOwnerId(
                tenantId,
                ownerType,
                ownerId
            );
        }
        else {
            responseList = keys.stream()
                .map(key -> metadataRepository.findByTenantIdAndOwnerTypeIgnoreCaseAndOwnerIdAndKeyNameIgnoreCase(
                    tenantId,
                    ownerType,
                    ownerId,
                    key))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        }

        MetadataResponse response = conversionService.convert(responseList, MetadataResponse.class);

        return Optional.ofNullable(response);
    }

    @Override
    public Page<MetadataSingleResponse> findAll(String tenantUrn, Integer page, Integer size) {

        Pageable pageable = new PageRequest(page, size);

        return findAllPage(tenantUrn, pageable);
    }

    private Page<MetadataSingleResponse> findAllPage(String tenantUrn, Pageable pageable) {

        Page<MetadataSingleResponse> result = MetadataPersistenceUtil.emptyPage();
        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            org.springframework.data.domain.Page<MetadataEntity> pageEntity = metadataRepository
                    .findByTenantId(tenantId, pageable);

            return conversionService.convert(pageEntity, result.getClass());
        }
        catch (IllegalArgumentException e) {
            log.warn("Error processing URN: Tenant URN '{}'", tenantUrn);
        }

        return result;
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

    private List<MetadataResponse> convert(List<MetadataEntity> entityList) {
        return entityList.stream()
            .map(o -> conversionService.convert(o, MetadataResponse.class))
            .collect(Collectors.toList());
    }
}
