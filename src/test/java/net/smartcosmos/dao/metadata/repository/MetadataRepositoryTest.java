package net.smartcosmos.dao.metadata.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import net.smartcosmos.dao.metadata.MetadataPersistenceConfig;
import net.smartcosmos.dao.metadata.MetadataPersistenceTestApplication;
import net.smartcosmos.dao.metadata.domain.MetadataDataType;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;
import net.smartcosmos.util.UuidUtil;

import static org.junit.Assert.*;

/**
 *
 * Sometimes these runtime created methods have issues that don't come up until they're
 * actually called. It's a minor setback with Spring, one that just requires some diligent
 * testing.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { MetadataPersistenceTestApplication.class, MetadataPersistenceConfig.class })
@ActiveProfiles("test")
@WebAppConfiguration
@IntegrationTest({ "spring.cloud.config.enabled=false", "eureka.client.enabled:false" })
public class MetadataRepositoryTest {

    @Autowired
    MetadataRepository metadataRepository;

    @Autowired
    MetadataOwnerRepository ownerRepository;

    private UUID tenantId;
    private UUID ownerId;
    private String ownerType = "Person";
    private String keyName = "test";

    private MetadataOwnerEntity owner;

    private Map<String, Object> keyValues;

    @Before
    public void setUp() throws Exception {

        ownerId = UuidUtil.getNewUuid();
        tenantId = UUID.randomUUID();

        owner = MetadataOwnerEntity.builder()
            .tenantId(tenantId)
            .type(ownerType)
            .id(ownerId)
            .build();

        MetadataEntity entity = MetadataEntity.builder()
            .owner(owner)
            .keyName(keyName)
            .value("true")
            .dataType(MetadataDataType.BOOLEAN)
            .tenantId(tenantId)
            .build();

        entity = metadataRepository.save(entity);
    }

    @Test
    public void thatDeleteIsSuccessful() throws Exception {

        List<MetadataEntity> entityList = metadataRepository.deleteByOwnerAndKeyNameIgnoreCase(owner, keyName);

        assertFalse(entityList.isEmpty());
        assertEquals(1, entityList.size());

        MetadataEntity entity = entityList.get(0);

        assertEquals("true", entity.getValue());
        assertEquals("Boolean", entity.getDataType().toString());
        assertEquals(keyName, entity.getKeyName());
    }

    @Test
    public void thatFindByKeyIsSuccessful() throws Exception {
        Optional<MetadataEntity> entity = metadataRepository.findByOwnerAndKeyNameIgnoreCase(owner, keyName);

        assertTrue(entity.isPresent());

        assertEquals("true", entity.get().getValue());
        assertEquals("Boolean", entity.get().getDataType().toString());
        assertEquals(keyName, entity.get().getKeyName());
    }

    @Test
    public void thatTypeAndKeyCaseInsensitive() throws Exception {
        Optional<MetadataEntity> entity = metadataRepository.findByOwnerAndKeyNameIgnoreCase(owner, keyName.toUpperCase());

        assertTrue(entity.isPresent());

        assertEquals("true", entity.get().getValue());
        assertEquals("Boolean", entity.get().getDataType().toString());
        assertEquals(keyName, entity.get().getKeyName());
    }
    @Test
    public void findByTenantIdPageable() throws Exception {

        final UUID tenantId = UUID.randomUUID();
        final int entityCount = 30;
        List<UUID> ids = new ArrayList<>();

        List<MetadataOwnerEntity> owners = new ArrayList<>();

        for (int i = 0; i < entityCount; i++) {
            UUID id = UUID.randomUUID();
            ids.add(id);

            MetadataOwnerEntity owner = MetadataOwnerEntity.builder()
                .tenantId(tenantId)
                .type("pageTest")
                .id(id)
                .build();

            owners.add(owner);

            MetadataEntity entity = metadataRepository
                .save(MetadataEntity.builder()
                    .owner(owner)
                    .dataType(MetadataDataType.BOOLEAN)
                    .keyName("pageTest")
                    .value("true")
                    .tenantId(tenantId)
                    .build());
        }


        Page<MetadataEntity> entityList = metadataRepository.findByOwnerIn(owners, new PageRequest(0, 1));
        assertFalse(entityList.getContent().isEmpty());

        assertEquals(1, entityList.getContent().size());
        assertEquals("true", entityList.getContent().get(0).getValue());
        assertEquals(entityCount, entityList.getTotalElements());
    }

}
