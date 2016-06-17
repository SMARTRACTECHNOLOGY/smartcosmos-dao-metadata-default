package net.smartcosmos.dao.metadata.repository;

import net.smartcosmos.dao.metadata.MetadataPersistenceConfig;
import net.smartcosmos.dao.metadata.MetadataPersistenceTestApplication;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.util.UuidUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    final UUID accountId = UUID.randomUUID();
    private UUID id;
    private UUID referenceId;
    private String entityReferenceType = "Object";
    private String key = "test";


    @Before
    public void setUp() throws Exception {

        referenceId = UuidUtil.getNewUuid();

        MetadataEntity entity = MetadataEntity.builder()
            .accountId(accountId)
            .referenceId(referenceId)
            .entityReferenceType(entityReferenceType)
            .rawValue("true")
            .dataType("BooleanType")
            .key(key)
            .build();

        entity = metadataRepository.save(entity);
        id = entity.getId();
    }

    @Test
    public void thatDeleteIsSuccessful() throws Exception {

        List<MetadataEntity> entityList = metadataRepository.deleteByAccountIdAndEntityReferenceTypeAndReferenceIdAndKey(accountId, entityReferenceType, referenceId, key);

        assertFalse(entityList.isEmpty());
        assertEquals(1, entityList.size());

        MetadataEntity entity = entityList.get(0);

        assertEquals("true", entity.getValue());
        assertEquals("BooleanType", entity.getDataType());
        assertEquals(key, entity.getKeyName());
    }

    @Test
    public void thatFindByKeyIsSuccessful() throws Exception {
        Optional<MetadataEntity> entity = metadataRepository.findByAccountIdAndEntityReferenceTypeAndReferenceIdAndKey(accountId, entityReferenceType, referenceId, key);

        assertTrue(entity.isPresent());

        assertEquals("true", entity.get().getValue());
        assertEquals("BooleanType", entity.get().getDataType());
        assertEquals(key, entity.get().getKeyName());
    }

}
