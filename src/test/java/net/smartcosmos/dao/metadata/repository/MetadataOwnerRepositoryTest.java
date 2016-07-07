package net.smartcosmos.dao.metadata.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { MetadataPersistenceTestApplication.class, MetadataPersistenceConfig.class })
@ActiveProfiles("test")
@WebAppConfiguration
@IntegrationTest({ "spring.cloud.config.enabled=false", "eureka.client.enabled:false" })
public class MetadataOwnerRepositoryTest {

    @Autowired
    MetadataOwnerRepository repository;

    private UUID tenantId;
    private UUID ownerId;
    private String ownerType = "Person";

    private Map<String, MetadataEntity> keyValues;

    @Before
    public void setUp() throws Exception {

        ownerId = UuidUtil.getNewUuid();
        tenantId = UUID.randomUUID();

        keyValues = new HashMap<>();
        for (int i= 0; i < 5; i++) {

            String key = "key" + String.valueOf(i);

            MetadataEntity metadata = MetadataEntity.builder()
                .keyName(key)
                .value("value" + String.valueOf(i))
                .dataType(MetadataDataType.STRING)
                .build();

            keyValues.put(key, metadata);
        }

        MetadataOwnerEntity entity = MetadataOwnerEntity.builder()
            .id(ownerId)
            .tenantId(tenantId)
            .type(ownerType)
            .build();

        entity = repository.save(entity);

        repository.addMetadataEntitiesToOwner(entity.getInternalId(), keyValues.values());
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
}
