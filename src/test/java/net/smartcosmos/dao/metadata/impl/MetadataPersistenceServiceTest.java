package net.smartcosmos.dao.metadata.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.smartcosmos.dao.metadata.MetadataPersistenceConfig;
import net.smartcosmos.dao.metadata.MetadataPersistenceTestApplication;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.repository.MetadataRepository;
import net.smartcosmos.dto.metadata.MetadataCreate;
import net.smartcosmos.dto.metadata.MetadataResponse;

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

    private final UUID tenantId = UUID.randomUUID();
    private final String tenantUrn = UuidUtil.getAccountUrnFromUuid(tenantId);

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
            .thenReturn(new SmartCosmosUser(tenantUrn, "urn:userUrn", "username",
                "password", Arrays.asList(new SimpleGrantedAuthority("USER"))));
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @After
    public void tearDown() throws Exception {
        metadataRepository.deleteAll();
    }

    // region Create

    @Test
    public void testCreate() throws Exception {

        final String ownerType = "Thing";
        final String ownerUrn = "urn:uuid:" + UuidUtil.getNewUuidAsString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        Object o = mapper.readTree("{\"x\":1,\"y\":2}");

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put("someBool", true);
        keyValues.put("someJson", o);
        keyValues.put("someNumber", 123);
        keyValues.put("someNull", null);
        keyValues.put("someString", "Text");

        MetadataCreate create = MetadataCreate.builder()
            .ownerType(ownerType)
            .ownerUrn(ownerUrn)
            .metadata(keyValues)
            .build();

        Optional<MetadataResponse> response = metadataPersistenceService.create(tenantUrn, create);

        assertTrue(response.isPresent());
        assertEquals(ownerType, response.get().getOwnerType());
        assertEquals(ownerUrn, response.get().getOwnerUrn());
        assertEquals(5, response.get().getMetadata().size());
        assertTrue(Boolean.parseBoolean(response.get().getMetadata().get("someBool").toString()));
        assertEquals("Text", response.get().getMetadata().get("someString").toString());

        ObjectNode output = (ObjectNode) response.get().getMetadata().get("someJson");
        assertEquals(1, output.findValue("x").asInt());
        assertEquals(2, output.findValue("y").asInt());

        List<MetadataEntity> entityList = metadataRepository.findByTenantIdAndOwnerTypeAndOwnerId(
            tenantId,
            ownerType,
            UuidUtil.getUuidFromUrn(ownerUrn));

        assertFalse(entityList.isEmpty());

        assertFalse(entityList.isEmpty());
        assertEquals(5, entityList.size());
    }

    @Test
    public void testCreateFailOnDuplicateKey() {

        final String ownerType = "Thing";
        final String ownerUrn = "urn:uuid:" + UuidUtil.getNewUuidAsString();

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put("duplicate", true);

        MetadataCreate create = MetadataCreate.builder()
            .ownerType(ownerType)
            .ownerUrn(ownerUrn)
            .metadata(keyValues)
            .build();

        Optional<MetadataResponse> response1 = metadataPersistenceService.create(tenantUrn, create);
        assertTrue(response1.isPresent());

        Optional<MetadataResponse> response2 = metadataPersistenceService.create(tenantUrn, create);
        assertFalse(response2.isPresent());
    }

    // endregion */

    // region update

    @Test
    public void testUpdate() throws Exception {

        final String keyName = "updateMe";
        final Boolean value = true;
        final String ownerType = "Thing";
        final String ownerUrn = "urn:uuid:" + UuidUtil.getNewUuidAsString();

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put(keyName, value);

        MetadataCreate create = MetadataCreate.builder()
            .ownerType(ownerType)
            .ownerUrn(ownerUrn)
            .metadata(keyValues)
            .build();

        metadataPersistenceService.create(tenantUrn, create);

        Optional<Object> o = metadataPersistenceService.findByKey(tenantUrn, ownerType, ownerUrn, keyName);

        assertTrue(o.isPresent());
        assertEquals(true, o.get());

        Optional<MetadataResponse> response = metadataPersistenceService.update(tenantUrn, ownerType, ownerUrn, keyName, false);

        assertTrue(response.isPresent());
        assertEquals(ownerType, response.get().getOwnerType());
        assertEquals(ownerUrn, response.get().getOwnerUrn());
        assertEquals(1, response.get().getMetadata().size());
        assertFalse(Boolean.parseBoolean(response.get().getMetadata().get("updateMe").toString()));

        o = metadataPersistenceService.findByKey(tenantUrn, ownerType, ownerUrn, keyName);

        assertTrue(o.isPresent());
        assertEquals(false, o.get());
    }

    // endregion */

    // region Delete

    @Test
    public void testDelete() {

        final String keyName = "deleteMe";
        final Boolean value = true;
        final String ownerType = "Thing";
        final String ownerUrn = "urn:uuid:" + UuidUtil.getNewUuidAsString();

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put(keyName, value);

        MetadataCreate create = MetadataCreate.builder()
            .ownerType(ownerType)
            .ownerUrn(ownerUrn)
            .metadata(keyValues)
            .build();

        metadataPersistenceService.create(tenantUrn, create);

        List<MetadataResponse> deleteList = metadataPersistenceService.delete(tenantUrn, ownerType, ownerUrn, keyName);

        assertFalse(deleteList.isEmpty());
        assertEquals(1, deleteList.size());
        assertEquals(ownerType, deleteList.get(0).getOwnerType());
        assertEquals(ownerUrn, deleteList.get(0).getOwnerUrn());
        assertEquals(1, deleteList.get(0).getMetadata().size());
        assertTrue(Boolean.parseBoolean(deleteList.get(0).getMetadata().get(keyName).toString()));
    }

    @Test
    public void testDeleteNonexistent() {

        final String keyName = "this-does-not-exist";
        final String ownerType = "Object";
        final String ownerUrn = "urn:uuid:" + UuidUtil.getNewUuidAsString();

        List<MetadataResponse> deleteList = metadataPersistenceService
            .delete(tenantUrn, ownerType, ownerUrn, keyName);

        assertTrue(deleteList.isEmpty());
    }

    // endregion */

    // region Find by Key

    @Test
    public void testFindByKey() {

        final String keyName = "findMe";
        final Boolean value = true;
        final String ownerType = "Thing";
        final String ownerUrn = "urn:uuid:" + UuidUtil.getNewUuidAsString();

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put(keyName, value);

        MetadataCreate create = MetadataCreate.builder()
            .ownerType(ownerType)
            .ownerUrn(ownerUrn)
            .metadata(keyValues)
            .build();

        metadataPersistenceService.create(tenantUrn, create);

        Optional<Object> response = metadataPersistenceService.findByKey(tenantUrn, ownerType, ownerUrn, keyName);

        assertTrue(response.isPresent());
        assertEquals(true, response.get());
    }

    @Test
    public void testFindByKeyNonexistent() {

        final String keyName = "this-does-not-exist";
        final String ownerType = "Thing";
        final String ownerUrn = "urn:uuid:" + UuidUtil.getNewUuidAsString();

        Optional<Object> response = metadataPersistenceService.findByKey(tenantUrn, ownerType, ownerUrn, keyName);

        assertFalse(response.isPresent());
    }

    // endregion */

    /* region Query

    @Test
    public void testFindBySingleSearchCriteriaKey() {

        populateQueryData();

        final String key = "searchMe";
        final String dataType = "BooleanType";
        final String rawValue = "true";
        final String entityReferenceType = "Object";
        final String referenceUrn = "urn:uuid:" + UuidUtil.getNewUuidAsString();

        MetadataCreate create = MetadataCreate.builder()
            .referenceUrn(referenceUrn)
            .entityReferenceType(entityReferenceType)
            .rawValue(rawValue)
            .dataType(dataType)
            .key(key)
            .build();

        List<MetadataCreate> createList = new ArrayList<>();
        createList.add(create);
        metadataPersistenceService.upsert(tenantUrn, createList);

        MetadataQuery query1 = MetadataQuery.builder()
            .key(key)
            .build();

        Collection<MetadataQuery> queryCollection = new ArrayList<>();
        queryCollection.add(query1);

        Set<MetadataQueryMatchResponse> responseList = metadataPersistenceService.findBySearchCriteria(tenantUrn,
            entityReferenceType,
            queryCollection,
            20);

        assertFalse(responseList.isEmpty());
        assertEquals(1, responseList.size());
    }

    @Test
    public void testFindBySingleSearchCriteriaRawValue() {

        populateQueryData();

        final String key = "searchMe";
        final String dataType = "IntegerType";
        final String rawValue = "42";
        final String entityReferenceType = "Object";
        final String referenceUrn = "urn:uuid:" + UuidUtil.getNewUuidAsString();

        MetadataCreate create = MetadataCreate.builder()
            .referenceUrn(referenceUrn)
            .entityReferenceType(entityReferenceType)
            .rawValue(rawValue)
            .dataType(dataType)
            .key(key)
            .build();

        List<MetadataCreate> createList = new ArrayList<>();
        createList.add(create);
        metadataPersistenceService.upsert(tenantUrn, createList);

        MetadataQuery query1 = MetadataQuery.builder()
            .rawValue(rawValue)
            .build();

        Collection<MetadataQuery> queryCollection = new ArrayList<>();
        queryCollection.add(query1);

        Set<MetadataQueryMatchResponse> responses = metadataPersistenceService.findBySearchCriteria(tenantUrn,
            entityReferenceType,
            queryCollection,
            20);

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(referenceUrn, responses.iterator().next().getUrn());
    }

    @Test
    public void testFindBySingleSearchCriteriaDataType() {

        populateQueryData();

        final String key = "searchMe";
        final String dataType = "DoubleType";
        final String rawValue = "23";
        final String entityReferenceType = "Object";
        final String referenceUrn = "urn:uuid:" + UuidUtil.getNewUuidAsString();

        MetadataCreate create = MetadataCreate.builder()
            .referenceUrn(referenceUrn)
            .entityReferenceType(entityReferenceType)
            .rawValue(rawValue)
            .dataType(dataType)
            .key(key)
            .build();

        List<MetadataCreate> createList = new ArrayList<>();
        createList.add(create);
        metadataPersistenceService.upsert(tenantUrn, createList);

        MetadataQuery query1 = MetadataQuery.builder()
            .dataType(dataType)
            .build();

        Collection<MetadataQuery> queryCollection = new ArrayList<>();
        queryCollection.add(query1);

        Set<MetadataQueryMatchResponse> responses = metadataPersistenceService.findBySearchCriteria(tenantUrn,
            entityReferenceType,
            queryCollection,
            20);

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(referenceUrn, responses.iterator().next().getUrn());
    }

    @Test
    public void testFindBySearchCriteriaNonexistent() {

        final String key = "does-not-exist";

        MetadataQuery query1 = MetadataQuery.builder()
            .key(key)
            .build();

        Collection<MetadataQuery> queryCollection = new ArrayList<>();
        queryCollection.add(query1);

        Set<MetadataQueryMatchResponse> responses = metadataPersistenceService.findBySearchCriteria(tenantUrn,
            "anyEntityReferenceType",
            queryCollection,
            20);

        assertTrue(responses.isEmpty());
    }

    @Test
    public void testFindByComplexSearchCriteriaDataTypeStringKeyA() {
        populateQueryData();

        MetadataQuery query1 = MetadataQuery.builder()
            .key(KEY_A)
            .dataType(DATA_TYPE_STRING)
            .build();

        Collection<MetadataQuery> queryCollection = new ArrayList<>();
        queryCollection.add(query1);

        Set<MetadataQueryMatchResponse> responseList = metadataPersistenceService.findBySearchCriteria(tenantUrn,
            ENTITY_REFERENCE_TYPE,
            queryCollection,
            20);

        assertFalse(responseList.isEmpty());
        assertEquals(2, responseList.size());

        List<UUID> uuidList = responseList.stream()
            .map(response -> UuidUtil.getUuidFromUrn(response.getUrn()))
            .collect(Collectors.toList());

        assertTrue(uuidList.contains(REFERENCE_ID_ONE));
        assertTrue(uuidList.contains(REFERENCE_ID_THREE));
    }

    @Test
    public void testFindByComplexSearchCriteriasDataTypeBooleanValueAAndKeyA() {
        populateQueryData();

        MetadataQuery query1 = MetadataQuery.builder()
            .dataType(DATA_TYPE_BOOLEAN)
            .rawValue(RAW_VALUE_BOOLEAN_A)
            .build();

        MetadataQuery query2 = MetadataQuery.builder()
            .key(KEY_A)
            .build();

        Collection<MetadataQuery> queryCollection = new ArrayList<>();
        queryCollection.add(query1);
        queryCollection.add(query2);

        Set<MetadataQueryMatchResponse> responses = metadataPersistenceService.findBySearchCriteria(tenantUrn,
            ENTITY_REFERENCE_TYPE,
            queryCollection,
            20);

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());

        List<UUID> uuidList = responses.stream()
            .map(response -> UuidUtil.getUuidFromUrn(response.getUrn()))
            .collect(Collectors.toList());

        assertTrue(uuidList.contains(REFERENCE_ID_TWO));
    }

    @Test
    public void testFindByComplexSearchCriteriaNothingMatches() {
        populateQueryData();

        MetadataQuery query1 = MetadataQuery.builder()
            .dataType(DATA_TYPE_BOOLEAN)
            .rawValue(RAW_VALUE_BOOLEAN_A)
            .build();

        MetadataQuery query2 = MetadataQuery.builder()
            .key(KEY_A)
            .build();

        MetadataQuery query3 = MetadataQuery.builder()
            .dataType(DATA_TYPE_STRING)
            .key(KEY_B)
            .build();

        Collection<MetadataQuery> queryCollection = new ArrayList<>();
        queryCollection.add(query1);
        queryCollection.add(query2);
        queryCollection.add(query3);

        Set<MetadataQueryMatchResponse> responses = metadataPersistenceService.findBySearchCriteria(tenantUrn,
            ENTITY_REFERENCE_TYPE,
            queryCollection,
            20);

        assertTrue(responses.isEmpty());
    }

    // endregion */

    /* region Count

    @Test
    public void testCountBySingleSearchCriteriaKey() {

        final String key = "searchMe";
        final String dataType = "BooleanType";
        final String rawValue = "true";
        final String entityReferenceType = "Object";
        final String referenceUrn = "urn:uuid:" + UuidUtil.getNewUuidAsString();

        MetadataCreate create = MetadataCreate.builder()
            .referenceUrn(referenceUrn)
            .entityReferenceType(entityReferenceType)
            .rawValue(rawValue)
            .dataType(dataType)
            .key(key)
            .build();

        List<MetadataCreate> createList = new ArrayList<>();
        createList.add(create);
        metadataPersistenceService.upsert(tenantUrn, createList);

        MetadataQuery query1 = MetadataQuery.builder()
            .key(key)
            .build();

        Collection<MetadataQuery> queryCollection = new ArrayList<>();
        queryCollection.add(query1);

        Long count = metadataPersistenceService.countBySearchCriteria(tenantUrn,
            ENTITY_REFERENCE_TYPE,
            queryCollection);

        assertNotNull(count);
        assertTrue(1L == count);
    }

    @Test
    public void testCountNonexistent() {
        final String key = "does-not-exist";

        MetadataQuery query1 = MetadataQuery.builder()
            .key(key)
            .build();

        Collection<MetadataQuery> queryCollection = new ArrayList<>();
        queryCollection.add(query1);

        Long count = metadataPersistenceService.countBySearchCriteria(tenantUrn, ENTITY_REFERENCE_TYPE, queryCollection);

        assertNotNull(count);
        assertTrue(0L == count);
    }

    // endregion

    // region Helper Methods

    private void populateQueryData() {

        MetadataEntity entity1 = MetadataEntity.builder()
            .tenantId(tenantId)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID_ONE)
            .key(KEY_ONE)
            .dataType(DATA_TYPE_ONE)
            .rawValue(RAW_VALUE_ONE)
            .build();

        MetadataEntity entity2 = MetadataEntity.builder()
            .tenantId(tenantId)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID_TWO)
            .key(KEY_TWO)
            .dataType(DATA_TYPE_TWO)
            .rawValue(RAW_VALUE_TWO)
            .build();

        MetadataEntity entity3 = MetadataEntity.builder()
            .tenantId(tenantId)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID_THREE)
            .key(KEY_THREE)
            .dataType(DATA_TYPE_THREE)
            .rawValue(RAW_VALUE_THREE)
            .build();

        MetadataEntity entity4 = MetadataEntity.builder()
            .tenantId(tenantId)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID_FOUR)
            .key(KEY_FOUR)
            .dataType(DATA_TYPE_FOUR)
            .rawValue(RAW_VALUE_FOUR)
            .build();

        MetadataEntity entity5 = MetadataEntity.builder()
            .tenantId(tenantId)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID_FIVE)
            .key(KEY_FIVE)
            .dataType(DATA_TYPE_FIVE)
            .rawValue(RAW_VALUE_FIVE)
            .build();

        MetadataEntity entity6 = MetadataEntity.builder()
            .tenantId(tenantId)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID_SIX)
            .key(KEY_SIX)
            .dataType(DATA_TYPE_SIX)
            .rawValue(RAW_VALUE_SIX)
            .build();

        metadataRepository.save(entity1);
        metadataRepository.save(entity2);
        metadataRepository.save(entity3);
        metadataRepository.save(entity4);
        metadataRepository.save(entity5);
        metadataRepository.save(entity6);
    }

    // endregion */
}
