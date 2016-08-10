package net.smartcosmos.dao.metadata.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import net.smartcosmos.dao.metadata.MetadataPersistenceConfig;
import net.smartcosmos.dao.metadata.MetadataPersistenceTestApplication;
import net.smartcosmos.dao.metadata.domain.MetadataDataType;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;
import net.smartcosmos.dao.metadata.util.MetadataValueParser;
import net.smartcosmos.util.UuidUtil;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { MetadataPersistenceTestApplication.class, MetadataPersistenceConfig.class })
@ActiveProfiles("test")
@WebAppConfiguration
@IntegrationTest({ "spring.cloud.config.enabled=false", "eureka.client.enabled:false" })
public class MetadataOwnerRepositoryTest {

    @Autowired
    MetadataOwnerRepository repository;

    @Autowired
    MetadataRepository metadataRepository;

    private UUID tenantId;
    private UUID ownerId;
    private String ownerType = "Person";
    private UUID internalId;
    private MetadataOwnerEntity owner;

    private Map<String, MetadataEntity> keyValues;

    @Before
    public void setUp() throws Exception {

        ownerId = UuidUtil.getNewUuid();
        tenantId = UUID.randomUUID();

        keyValues = new HashMap<>();
        for (int i = 0; i < 5; i++) {

            String key = "key" + String.valueOf(i);

            MetadataEntity metadata = MetadataEntity.builder()
                .keyName(key)
                .value("value" + String.valueOf(i))
                .dataType(MetadataDataType.STRING)
                .build();

            keyValues.put(key, metadata);
        }

        owner = MetadataOwnerEntity.builder()
            .id(ownerId)
            .tenantId(tenantId)
            .type(ownerType)
            .build();

        owner = repository.save(owner);
        internalId = owner.getInternalId();

        repository.addMetadataEntitiesToOwner(internalId, keyValues.values());
    }

    @After
    public void tearDown() throws Exception {

        repository.deleteAll();
    }

    @Test
    public void findByTenantIdAndTypeIgnoreCaseAndId() throws Exception {

        Optional<MetadataOwnerEntity> entity = repository.findByTenantIdAndTypeIgnoreCaseAndId(tenantId, ownerType, ownerId);
        assertTrue(entity.isPresent());
    }

    @Test
    public void orphanDeleteEmptyMapDeletes() throws Exception {

        UUID ownerId = UuidUtil.getNewUuid();
        UUID tenantId = UUID.randomUUID();

        MetadataOwnerEntity entity = MetadataOwnerEntity.builder()
            .id(ownerId)
            .tenantId(tenantId)
            .type(ownerType)
            .build();
        repository.save(entity);

        repository.orphanDelete(tenantId, ownerType, ownerId);

        Optional<MetadataOwnerEntity> owner = repository.findByTenantIdAndTypeIgnoreCaseAndId(tenantId, ownerType, ownerId);
        assertFalse(owner.isPresent());
    }

    @Test
    public void orphanDeleteNonEmptyMapDeletesNot() throws Exception {

        repository.orphanDelete(tenantId, ownerType, ownerId);

        Optional<MetadataOwnerEntity> entity = repository.findByTenantIdAndTypeIgnoreCaseAndId(tenantId, ownerType, ownerId);
        assertTrue(entity.isPresent());
    }

    @Test
    public void addMetadataEntitiesToOwner() throws Exception {

        String newKey = "newKey";

        MetadataEntity metadata = MetadataEntity.builder()
            .keyName(newKey)
            .value("value")
            .dataType(MetadataDataType.STRING)
            .build();

        Collection<MetadataEntity> metadataEntities = new HashSet<>();
        metadataEntities.add(metadata);

        repository.addMetadataEntitiesToOwner(internalId, metadataEntities);

        Map<String, MetadataEntity> associatedMetadata = repository.getAssociatedMetadataEntities(internalId);
        assertTrue(associatedMetadata.containsKey(newKey));
    }

    @Test
    public void getAssociatedMetadataEntities() throws Exception {

        Map<String, MetadataEntity> metadataEntities = repository.getAssociatedMetadataEntities(internalId);

        assertEquals(5, metadataEntities.size());
        assertTrue(metadataEntities.containsKey("key0"));
        assertTrue(metadataEntities.containsKey("key1"));
        assertTrue(metadataEntities.containsKey("key2"));
        assertTrue(metadataEntities.containsKey("key3"));
        assertTrue(metadataEntities.containsKey("key4"));
    }

    @Test
    public void updateMetadataEntity() throws Exception {

        final String key = "key0";
        final String newValue = MetadataValueParser.getValue(true);
        final MetadataDataType newDataType = MetadataDataType.BOOLEAN;

        MetadataEntity updataMetadataEntity = MetadataEntity.builder()
            .keyName(key)
            .value(newValue)
            .dataType(newDataType)
            .owner(owner)
            .build();

        Optional<MetadataEntity> update = repository.updateMetadataEntity(internalId, updataMetadataEntity);

        assertTrue(update.isPresent());

        Optional<MetadataEntity> savedMetadataEntity = metadataRepository.findByOwner_TenantIdAndOwner_TypeAndOwner_IdAndKeyNameIgnoreCase(owner
                                                                                                                                               .getTenantId(),
                                                                                                                                           owner
                                                                                                                                               .getType(),
                                                                                                                                           owner
                                                                                                                                               .getId(),
                                                                                                                                           key);

        assertTrue(savedMetadataEntity.isPresent());
        assertEquals(key,
                     savedMetadataEntity.get()
                         .getKeyName());
        assertEquals(newValue,
                     savedMetadataEntity.get()
                         .getValue());
        assertEquals(newDataType,
                     savedMetadataEntity.get()
                         .getDataType());
    }

    @Test
    public void updateMetadataEntityNonExistentKey() throws Exception {

        MetadataEntity updataMetadataEntity = MetadataEntity.builder()
            .keyName("Does not exist")
            .value("value1")
            .dataType(MetadataDataType.STRING)
            .owner(owner)
            .build();

        Optional<MetadataEntity> update = repository.updateMetadataEntity(internalId, updataMetadataEntity);

        assertFalse(update.isPresent());
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void updateMetadataEntityNonExistentOwner() throws Exception {

        MetadataEntity updataMetadataEntity = MetadataEntity.builder()
            .keyName("Does not exist")
            .value("value1")
            .dataType(MetadataDataType.STRING)
            .owner(owner)
            .build();

        Optional<MetadataEntity> update = repository.updateMetadataEntity(UUID.randomUUID(), updataMetadataEntity);
    }
}
