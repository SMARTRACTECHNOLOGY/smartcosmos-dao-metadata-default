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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    private UUID tenantId;
    private UUID ownerId;
    private String ownerType = "Thing";
    private String keyName = "test";

    private Map<String, Object> keyValues;

    @Before
    public void setUp() throws Exception {

        ownerId = UuidUtil.getNewUuid();
        tenantId = UUID.randomUUID();

        MetadataEntity entity = MetadataEntity.builder()
            .tenantId(tenantId)
            .ownerId(ownerId)
            .ownerType(ownerType)
            .keyName(keyName)
            .value("true")
            .dataType("Boolean")
            .build();

        entity = metadataRepository.save(entity);
    }

    @Test
    public void thatDeleteIsSuccessful() throws Exception {

        List<MetadataEntity> entityList = metadataRepository.deleteByTenantIdAndOwnerTypeAndOwnerIdAndKeyName(
            tenantId,
            ownerType,
            ownerId,
            keyName);

        assertFalse(entityList.isEmpty());
        assertEquals(1, entityList.size());

        MetadataEntity entity = entityList.get(0);

        assertEquals("true", entity.getValue());
        assertEquals("Boolean", entity.getDataType());
        assertEquals(keyName, entity.getKeyName());
    }

    @Test
    public void thatFindByKeyIsSuccessful() throws Exception {
        Optional<MetadataEntity> entity = metadataRepository.findByTenantIdAndOwnerTypeAndOwnerIdAndKeyName(
            tenantId,
            ownerType,
            ownerId,
            keyName);

        assertTrue(entity.isPresent());

        assertEquals("true", entity.get().getValue());
        assertEquals("Boolean", entity.get().getDataType());
        assertEquals(keyName, entity.get().getKeyName());
    }

}
