package net.smartcosmos.dao.metadata.impl;

import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.metadata.MetadataDao;
import net.smartcosmos.dao.metadata.repository.MetadataRepository;
import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.dto.metadata.MetadataUpsert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolationException;
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
    public List<MetadataResponse> upsert(String accountUrn, String entityReferenceType, String referenceUrn, List<MetadataUpsert> metadataUpsertList) throws ConstraintViolationException {
        return null;
    }

    @Override
    public Optional<MetadataResponse> delete(String accountUrn, String entityReferenceType, String referenceUrn, String key) {
        return null;
    }

    @Override
    public Optional<MetadataResponse> findByKey(String accountUrn, String entityReferenceType, String referenceUrn, String key) {
        return null;
    }
}
