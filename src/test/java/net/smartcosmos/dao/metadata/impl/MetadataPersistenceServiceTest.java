package net.smartcosmos.dao.metadata.impl;

import net.smartcosmos.dao.metadata.MetadataPersistenceConfig;
import net.smartcosmos.dao.metadata.MetadataPersistenceTestApplication;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.repository.MetadataRepository;
import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.dto.metadata.MetadataUpsert;
import net.smartcosmos.security.user.SmartCosmosUser;
import net.smartcosmos.util.UuidUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.*;

import static org.junit.Assert.*;

@SuppressWarnings("Duplicates")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { MetadataPersistenceTestApplication.class, MetadataPersistenceConfig.class })
@ActiveProfiles("test")
@WebAppConfiguration
@IntegrationTest({ "spring.cloud.config.enabled=false", "eureka.client.enabled:false" })
public class MetadataPersistenceServiceTest {

    private final UUID accountId = UUID.randomUUID();
    private final String accountUrn = UuidUtil.getAccountUrnFromUuid(accountId);

    @Autowired
    MetadataPersistenceService metadataPersistenceService;

    @Autowired
    MetadataRepository metadataRepository;

    @Before
    public void setUp() throws Exception {

        // Need to mock out user for conversion service.
        // Might be a good candidate for a test package util.
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal())
            .thenReturn(new SmartCosmosUser(accountUrn, "urn:userUrn", "username",
                "password", Arrays.asList(new SimpleGrantedAuthority("USER"))));
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @After
    public void tearDown() throws Exception {
        metadataRepository.deleteAll();
    }

    @Test
    public void testCreate() throws Exception {

        final String key = "key";
        final String dataType = "BooleanType";
        final String rawValue = "true";
        final String entityReferenceType = "Object";
        final String referenceUrn = "urn:uuid:" + UuidUtil.getNewUuidAsString();

        MetadataUpsert create = MetadataUpsert.builder()
            .referenceUrn(referenceUrn)
            .entityReferenceType(entityReferenceType)
            .rawValue(rawValue)
            .dataType(dataType)
            .key(key)
            .build();

        List<MetadataUpsert> createList = new ArrayList<>();
        createList.add(create);

        List<MetadataResponse> responseList = metadataPersistenceService.upsert(accountUrn, createList);

        assertFalse(responseList.isEmpty());
        assertEquals(1, responseList.size());
        assertEquals(referenceUrn, responseList.get(0).getReferenceUrn());
        assertEquals(entityReferenceType, responseList.get(0).getEntityReferenceType());
        assertEquals(key, responseList.get(0).getKey());
        assertEquals(dataType, responseList.get(0).getDataType());
        assertEquals(rawValue, responseList.get(0).getRawValue());

        List<MetadataEntity> entityList = metadataRepository.findByAccountIdAndReferenceId(accountId, UuidUtil.getUuidFromUrn(referenceUrn));

        assertFalse(entityList.isEmpty());

        assertFalse(entityList.isEmpty());
        assertEquals(1, entityList.size());
        assertEquals(referenceUrn, UuidUtil.getUrnFromUuid(entityList.get(0).getReferenceId()));
        assertEquals(entityReferenceType, entityList.get(0).getEntityReferenceType());
        assertEquals(key, entityList.get(0).getKey());
        assertEquals(dataType, entityList.get(0).getDataType());
        assertEquals(rawValue, entityList.get(0).getRawValue());
    }

    @Test
    public void testUpdate() throws Exception {

        final String key = "updateKey";
        final String dataType = "BooleanType";
        final String initialRawValue = "true";
        final String updateRawValue = "false";
        final String entityReferenceType = "Object";
        final String referenceUrn = "urn:uuid:" + UuidUtil.getNewUuidAsString();

        MetadataUpsert create = MetadataUpsert.builder()
            .referenceUrn(referenceUrn)
            .entityReferenceType(entityReferenceType)
            .rawValue(initialRawValue)
            .dataType(dataType)
            .key(key)
            .build();

        List<MetadataUpsert> createList = new ArrayList<>();
        createList.add(create);

        List<MetadataResponse> createResponseList = metadataPersistenceService.upsert(accountUrn, createList);

        assertFalse(createResponseList.isEmpty());
        assertEquals(1, createResponseList.size());
        assertEquals(referenceUrn, createResponseList.get(0).getReferenceUrn());
        assertEquals(entityReferenceType, createResponseList.get(0).getEntityReferenceType());
        assertEquals(key, createResponseList.get(0).getKey());
        assertEquals(dataType, createResponseList.get(0).getDataType());
        assertEquals(initialRawValue, createResponseList.get(0).getRawValue());

        MetadataUpsert update = MetadataUpsert.builder()
            .referenceUrn(referenceUrn)
            .entityReferenceType(entityReferenceType)
            .rawValue(updateRawValue)
            .dataType(dataType)
            .key(key)
            .build();

        List<MetadataUpsert> updateList = new ArrayList<>();
        updateList.add(update);

        List<MetadataResponse> updateResponseList = metadataPersistenceService.upsert(accountUrn, updateList);

        assertFalse(updateResponseList.isEmpty());
        assertEquals(1, updateResponseList.size());
        assertEquals(referenceUrn, updateResponseList.get(0).getReferenceUrn());
        assertEquals(entityReferenceType, updateResponseList.get(0).getEntityReferenceType());
        assertEquals(key, updateResponseList.get(0).getKey());
        assertEquals(dataType, updateResponseList.get(0).getDataType());
        assertEquals(updateRawValue, updateResponseList.get(0).getRawValue());

        List<MetadataEntity> entityList = metadataRepository.findByAccountIdAndReferenceId(accountId, UuidUtil.getUuidFromUrn(referenceUrn));

        assertFalse(entityList.isEmpty());

        assertFalse(entityList.isEmpty());
        assertEquals(1, entityList.size());
        assertEquals(referenceUrn, UuidUtil.getUrnFromUuid(entityList.get(0).getReferenceId()));
        assertEquals(entityReferenceType, entityList.get(0).getEntityReferenceType());
        assertEquals(key, entityList.get(0).getKey());
        assertEquals(dataType, entityList.get(0).getDataType());
        assertEquals(updateRawValue, entityList.get(0).getRawValue());
    }

    @Test
    public void testDelete() {

        final String key = "deleteMe";
        final String dataType = "BooleanType";
        final String rawValue = "true";
        final String entityReferenceType = "Object";
        final String referenceUrn = "urn:uuid:" + UuidUtil.getNewUuidAsString();

        MetadataUpsert create = MetadataUpsert.builder()
            .referenceUrn(referenceUrn)
            .entityReferenceType(entityReferenceType)
            .rawValue(rawValue)
            .dataType(dataType)
            .key(key)
            .build();

        List<MetadataUpsert> createList = new ArrayList<>();
        createList.add(create);
        metadataPersistenceService.upsert(accountUrn, createList);

        List<MetadataResponse> deleteList = metadataPersistenceService.delete(accountUrn, entityReferenceType, referenceUrn, key);

        assertFalse(deleteList.isEmpty());
        assertEquals(1, deleteList.size());
        assertEquals(referenceUrn, deleteList.get(0).getReferenceUrn());
        assertEquals(entityReferenceType, deleteList.get(0).getEntityReferenceType());
        assertEquals(key, deleteList.get(0).getKey());
        assertEquals(dataType, deleteList.get(0).getDataType());
        assertEquals(rawValue, deleteList.get(0).getRawValue());
    }

    @Test
    public void testFindByKey() {

        final String key = "findMe";
        final String dataType = "BooleanType";
        final String rawValue = "true";
        final String entityReferenceType = "Object";
        final String referenceUrn = "urn:uuid:" + UuidUtil.getNewUuidAsString();

        MetadataUpsert create = MetadataUpsert.builder()
            .referenceUrn(referenceUrn)
            .entityReferenceType(entityReferenceType)
            .rawValue(rawValue)
            .dataType(dataType)
            .key(key)
            .build();

        List<MetadataUpsert> createList = new ArrayList<>();
        createList.add(create);
        metadataPersistenceService.upsert(accountUrn, createList);

        Optional<MetadataResponse> response = metadataPersistenceService.findByKey(accountUrn, entityReferenceType, referenceUrn, key);

        assertTrue(response.isPresent());
        assertEquals(referenceUrn, response.get().getReferenceUrn());
        assertEquals(entityReferenceType, response.get().getEntityReferenceType());
        assertEquals(key, response.get().getKey());
        assertEquals(dataType, response.get().getDataType());
        assertEquals(rawValue, response.get().getRawValue());
    }
}
