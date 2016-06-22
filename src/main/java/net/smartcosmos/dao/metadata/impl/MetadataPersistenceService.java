package net.smartcosmos.dao.metadata.impl;

import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.metadata.MetadataDao;
import net.smartcosmos.dao.metadata.converter.MetadataEntityListToMetadataResponseConverter;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.repository.MetadataRepository;
import net.smartcosmos.dao.metadata.util.MetadataValueParser;
import net.smartcosmos.dto.metadata.MetadataCreate;
import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.dto.metadata.MetadataSingleResponse;
import net.smartcosmos.util.UuidUtil;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MetadataPersistenceService implements MetadataDao {

    public static final int NO_LIMIT_ON_QUERY_RESPONSE_SIZE = 0;

    private final MetadataRepository metadataRepository;
    private final ConversionService conversionService;

    @Autowired
    public MetadataPersistenceService(MetadataRepository metadataRepository,
                                      ConversionService conversionService) {
        this.metadataRepository = metadataRepository;
        this.conversionService = conversionService;
    }

    @Override
    public Optional<MetadataResponse> create(String tenantUrn, MetadataCreate createMetadata)
        throws ConstraintViolationException {

        UUID tenantId = UuidUtil.getUuidFromAccountUrn(tenantUrn);

        List<String> keys = new ArrayList<>();
        keys.addAll(createMetadata.getMetadata().keySet());

        Long count = metadataRepository.countByTenantIdAndOwnerTypeAndOwnerIdAndKeyNameIn(
            tenantId,
            createMetadata.getOwnerType(),
            UuidUtil.getUuidFromUrn(createMetadata.getOwnerUrn()),
            keys);

        if (count > 0) {
            return Optional.empty();
        }

        List<MetadataEntity> responseList = new ArrayList<>();
        MetadataEntity[] entities = conversionService.convert(createMetadata, MetadataEntity[].class);

        for (MetadataEntity entity : entities) {
            entity.setTenantId(tenantId);
            entity = persist(entity);
            responseList.add(entity);
        }
        MetadataResponse response = new MetadataEntityListToMetadataResponseConverter()
                .convert(responseList);
        return Optional.ofNullable(response);
    }

    @Override
    public Optional<MetadataResponse> upsert(String tenantUrn, MetadataCreate upsertMetadata)
        throws ConstraintViolationException {

        UUID tenantId = UuidUtil.getUuidFromAccountUrn(tenantUrn);

        List<MetadataEntity> responseList = new ArrayList<>();
        MetadataEntity[] entities = conversionService.convert(upsertMetadata, MetadataEntity[].class);

        for (MetadataEntity entity : entities) {

            Optional<MetadataEntity> existingEntity = metadataRepository.findByTenantIdAndOwnerTypeAndOwnerIdAndKeyName(
                entity.getTenantId(),
                entity.getOwnerType(),
                entity.getOwnerId(),
                entity.getKeyName());
            if (existingEntity.isPresent()) {
                entity.setId(existingEntity.get().getId());
            }
            entity.setTenantId(tenantId);
            entity = persist(entity);
            responseList.add(entity);
        }

        MetadataResponse response = new MetadataEntityListToMetadataResponseConverter()
            .convert(responseList);
        return Optional.ofNullable(response);
    }

    @Override
    public Optional<MetadataResponse> update(
        String tenantUrn,
        String ownerType,
        String ownerUrn,
        String key,
        Object value)
        throws ConstraintViolationException {

        UUID tenantId = UuidUtil.getUuidFromAccountUrn(tenantUrn);
        UUID ownerId = UuidUtil.getUuidFromUrn(ownerUrn);

        Optional<MetadataEntity> entity = metadataRepository.findByTenantIdAndOwnerTypeAndOwnerIdAndKeyName(
            tenantId,
            ownerType,
            ownerId,
            key);

        if (entity.isPresent()) {
            MetadataEntity updateEntity = entity.get();
            updateEntity.setDataType(value != null ? value.getClass().getSimpleName() : "null");
            updateEntity.setValue(value != null ? value.toString() : null);
            updateEntity = persist(updateEntity);
            MetadataResponse response = conversionService.convert(updateEntity, MetadataResponse.class);
            return Optional.ofNullable(response);
        }
        return Optional.empty();
    }

    @Override
    public List<MetadataResponse> delete(String tenantUrn, String ownerType, String ownerUrn, String key) {

        UUID tenantId = UuidUtil.getUuidFromAccountUrn(tenantUrn);
        List<MetadataEntity> deleteList = new ArrayList<>();

        try {
            UUID ownerId = UuidUtil.getUuidFromUrn(ownerUrn);
            deleteList = metadataRepository.deleteByTenantIdAndOwnerTypeAndOwnerIdAndKeyName(
                tenantId,
                ownerType,
                ownerId,
                key);
        } catch (IllegalArgumentException e) {
            // empty list will be returned anyway
            log.warn("Illegal URN submitted: %s by tenant %s", ownerUrn, tenantUrn);
        }

        return deleteList.stream()
            .map(o -> conversionService.convert(o, MetadataResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    public List<MetadataResponse> deleteAllByOwner(String tenantUrn, String ownerType, String ownerUrn) {

        UUID tenantId = UuidUtil.getUuidFromAccountUrn(tenantUrn);
        List<MetadataEntity> deleteList = new ArrayList<>();

        try {
            UUID ownerId = UuidUtil.getUuidFromUrn(ownerUrn);
            deleteList = metadataRepository.deleteByTenantIdAndOwnerTypeAndOwnerId(
                tenantId,
                ownerType,
                ownerId);
        } catch (IllegalArgumentException e) {
            // empty list will be returned anyway
            log.warn("Illegal URN submitted: %s by tenant %s", ownerUrn, tenantUrn);
        }

        return deleteList.stream()
            .map(o -> conversionService.convert(o, MetadataResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Object> findByKey(String tenantUrn, String ownerType, String ownerUrn, String key) {

        Optional<MetadataEntity> entity = Optional.empty();
        try {
            UUID tenantId = UuidUtil.getUuidFromAccountUrn(tenantUrn);
            UUID ownerId = UuidUtil.getUuidFromUrn(ownerUrn);

            entity = metadataRepository.findByTenantIdAndOwnerTypeAndOwnerIdAndKeyName(
                tenantId,
                ownerType,
                ownerId,
                key);
        } catch (IllegalArgumentException e) {
            // empty Optional will be returned anyway
            log.warn("Illegal URN submitted: %s by account %s", ownerUrn, tenantUrn);
        }

        if (entity.isPresent())
        {
            Object o = MetadataValueParser.parseValue(entity.get());
            return Optional.ofNullable(o);
        }
        return Optional.empty();
    }

    @Override
    public Optional<MetadataResponse> findByOwner(
        String tenantUrn,
        String ownerType,
        String ownerUrn,
        Collection<String> keys) {

        UUID tenantId = UuidUtil.getUuidFromAccountUrn(tenantUrn);
        UUID ownerId = UuidUtil.getUuidFromUrn(ownerUrn);

        if (keys.isEmpty()) {
            try {
                List<MetadataEntity> responseList = metadataRepository.findByTenantIdAndOwnerTypeAndOwnerId(
                    tenantId,
                    ownerType,
                    ownerId
                );
                MetadataResponse response = new MetadataEntityListToMetadataResponseConverter()
                    .convert(responseList);
                if (response != null) {
                    return Optional.of(response);
                }
            } catch (IllegalArgumentException e) {
                // empty Optional will be returned anyway
                log.warn("Illegal URN submitted: %s by account %s", ownerUrn, tenantUrn);
            }
        } else {
            try {
                List<MetadataEntity> responseList = new ArrayList<>();
                for (String keyName: keys) {
                    Optional<MetadataEntity> entity = metadataRepository.findByTenantIdAndOwnerTypeAndOwnerIdAndKeyName(
                        tenantId,
                        ownerType,
                        ownerId,
                        keyName);
                    if (entity.isPresent()) {
                        responseList.add(entity.get());
                    }
                }
                MetadataResponse response = new MetadataEntityListToMetadataResponseConverter()
                    .convert(responseList);
                if (response != null) {
                    return Optional.of(response);
                }
            } catch (IllegalArgumentException e) {
                // empty Optional will be returned anyway
                log.warn("Illegal URN submitted: %s by account %s", ownerUrn, tenantUrn);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<MetadataSingleResponse> findAll(String tenantUrn, Long page, Integer size) {
        // TODO: ...
        throw new RuntimeException("Not implemented yet");
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
