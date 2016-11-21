package net.smartcosmos.dao.metadata.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
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

import net.smartcosmos.dao.metadata.MetadataPersistenceConfig;
import net.smartcosmos.dao.metadata.MetadataPersistenceTestApplication;
import net.smartcosmos.dao.metadata.SortOrder;
import net.smartcosmos.dao.metadata.domain.MetadataDataType;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;
import net.smartcosmos.dao.metadata.repository.MetadataRepository;
import net.smartcosmos.dao.metadata.util.UuidUtil;
import net.smartcosmos.dto.metadata.MetadataOwnerResponse;
import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.dto.metadata.MetadataSingleResponse;
import net.smartcosmos.dto.metadata.MetadataValueResponse;
import net.smartcosmos.dto.metadata.Page;
import net.smartcosmos.security.user.SmartCosmosUser;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { MetadataPersistenceTestApplication.class, MetadataPersistenceConfig.class })
@ActiveProfiles("test")
@WebAppConfiguration
@IntegrationTest({ "spring.cloud.config.enabled=false", "eureka.client.enabled:false" })
public class MetadataPersistenceServiceTest {

    private final UUID tenantId = UUID.randomUUID();
    private final String tenantUrn = UuidUtil.getTenantUrnFromUuid(tenantId);

    private final UUID tenantId2 = UUID.randomUUID();
    private final String tenantUrn2 = UuidUtil.getTenantUrnFromUuid(tenantId);

    private static final String[] urns = {
        "urn:thing:uuid:8614fac9-693d-4bee-886f-f9eefd60180a",
        "urn:thing:uuid:73f81ca4-0800-4769-bb6f-db4a61b0fea1",
        "urn:thing:uuid:2650c7d5-9dc5-4455-9eda-e34066050f76",
        "urn:thing:uuid:10857865-4230-4d27-9dcc-9ef17ace3921",
        "urn:thing:uuid:28124b54-edc4-4eed-b307-2af056119db2",
        "urn:thing:uuid:fe55664a-0896-42bb-86c6-ba0c668eb348",
        "urn:thing:uuid:9f1e8d9e-8d8e-4d04-b776-bbfcdf760405",
        "urn:thing:uuid:71a03bc2-d41a-4c08-a355-6d0d5dc6f8a4",
        "urn:thing:uuid:17f890b8-caa6-4ad2-98d6-641f3999128e",
        "urn:thing:uuid:5926a69f-ff56-4a9c-ab79-e90b829f2ec4",
        "urn:thing:uuid:97005fff-da53-4ad2-8a40-128da31e9cd4",
        "urn:thing:uuid:4d872a55-ea69-4b9f-935a-9d21303085a5" };

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
        Mockito.when(securityContext.getAuthentication())
            .thenReturn(authentication);
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
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        final Boolean bool = true;
        final JSONObject jsonObject = new JSONObject("{\"x\":1,\"y\":2}");
        final JSONArray jsonArray = new JSONArray("[\"x\",\"y\"]");
        final Number number = 123;
        final String text = "Text";

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put("someBool", bool);
        keyValues.put("someJsonObject", jsonObject);
        keyValues.put("someJsonArray", jsonArray);
        keyValues.put("someNumber", number);
        keyValues.put("someNull", null);
        keyValues.put("someString", text);

        Optional<MetadataResponse> response = metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);

        assertTrue(response.isPresent());
        assertEquals(ownerType,
                     response.get()
                         .getOwnerType());
        assertEquals(ownerUrn,
                     response.get()
                         .getOwnerUrn());

        assertEquals(6,
                     response.get()
                         .getMetadata()
                         .size());

        assertEquals(bool,
                     response.get()
                         .getMetadata()
                         .get("someBool"));
        assertEquals(jsonObject.toString(),
                     response.get()
                         .getMetadata()
                         .get("someJsonObject")
                         .toString());
        assertEquals(jsonArray.toString(),
                     response.get()
                         .getMetadata()
                         .get("someJsonArray")
                         .toString());
        assertEquals(number,
                     response.get()
                         .getMetadata()
                         .get("someNumber"));
        assertEquals(NullNode.getInstance(),
                     response.get()
                         .getMetadata()
                         .get("someNull"));
        assertEquals(text,
                     response.get()
                         .getMetadata()
                         .get("someString"));

        List<MetadataEntity> entityList = metadataRepository.findByOwner_TenantIdAndOwner_TypeAndOwner_Id(tenantId,
                                                                                                          ownerType,
                                                                                                          UuidUtil.getUuidFromUrn(ownerUrn));

        assertFalse(entityList.isEmpty());

        assertFalse(entityList.isEmpty());
        assertEquals(6, entityList.size());
    }

    @Test
    public void testCreateWithComplexJsonMetadataValue() throws Exception {

        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        final JSONObject jsonObject = new JSONObject("{ \"frog\" : \"bog\", \"log\" : { \"dog\" : \"hog\", \"flog\" : { \"grog\" : \"mog\" } } }");
        final String jsonAsString = "{ \"frog\" : \"bog\", \"log\" : { \"dog\" : \"hog\", \"flog\" : { \"grog\" : \"mog\" } } }";

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put("someJsonObject", jsonObject);
        keyValues.put("someJsonObjectAsString", jsonAsString);

        Optional<MetadataResponse> response = metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);

        assertTrue(response.isPresent());
        assertEquals(ownerType,
                     response.get()
                         .getOwnerType());
        assertEquals(ownerUrn,
                     response.get()
                         .getOwnerUrn());

        assertEquals(2,
                     response.get()
                         .getMetadata()
                         .size());

        assertEquals(jsonObject.toString(),
                     response.get()
                         .getMetadata()
                         .get("someJsonObject")
                         .toString());

        assertEquals(jsonAsString,
                     response.get()
                         .getMetadata()
                         .get("someJsonObjectAsString")
                         .toString());

        List<MetadataEntity> entityList = metadataRepository.findByOwner_TenantIdAndOwner_TypeAndOwner_Id(tenantId,
                                                                                                          ownerType,
                                                                                                          UuidUtil.getUuidFromUrn(ownerUrn));

        assertFalse(entityList.isEmpty());

        assertFalse(entityList.isEmpty());
        assertEquals(2, entityList.size());
    }

    @Test
    public void testCreateWithComplexJsonMetadataValue2() throws Exception {

        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        final JSONObject jsonObject = new JSONObject("{\n"
                                                     + "    \"minH\": 6,\n"
                                                     + "    \"minW\": 6,\n"
                                                     + "    \"x\": 0,\n"
                                                     + "    \"y\": 0,\n"
                                                     + "    \"w\": 6,\n"
                                                     + "    \"h\": 6,\n"
                                                     + "    \"static\": false,\n"
                                                     + "    \"state\": {\n"
                                                     + "        \"type\": \"\",\n"
                                                     + "        \"title\": \"\",\n"
                                                     + "        \"configuration\": {\n"
                                                     + "            \"chartType\": \"bar\"\n"
                                                     + "        }\n"
                                                     + "     }\n"
                                                     + "   }\n"
                                                     + "    \"maxH\": 8,\n"
                                                     + "    \"maxW\": 8,\n"
                                                     + "}");

        final String jsonAsString = "{\n"
                                    + "    \"minH\": 6,\n"
                                    + "    \"minW\": 6,\n"
                                    + "    \"x\": 0,\n"
                                    + "    \"y\": 0,\n"
                                    + "    \"w\": 6,\n"
                                    + "    \"h\": 6,\n"
                                    + "    \"static\": false,\n"
                                    + "    \"state\": {\n"
                                    + "        \"type\": \"\",\n"
                                    + "        \"title\": \"\",\n"
                                    + "        \"configuration\": {\n"
                                    + "            \"chartType\": \"bar\"\n"
                                    + "        }\n"
                                    + "     }\n"
                                    + "   }\n"
                                    + "    \"maxH\": 8,\n"
                                    + "    \"maxW\": 8,\n"
                                    + "}";

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put("someJsonObjectAsString", jsonAsString);
        keyValues.put("someJsonObject", jsonObject);

        Optional<MetadataResponse> response = metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);

        Optional<MetadataValueResponse> getResponseJsonObject = metadataPersistenceService.findByKey(tenantUrn, ownerType, ownerUrn, "someJsonObject");
        Optional<MetadataValueResponse> getResponseJsonObjectAsString = metadataPersistenceService.findByKey(tenantUrn, ownerType, ownerUrn, "someJsonObjectAsString");

        assertTrue(response.isPresent());
        assertEquals(ownerType,
                     response.get()
                         .getOwnerType());
        assertEquals(ownerUrn,
                     response.get()
                         .getOwnerUrn());

        assertEquals(2,
                     response.get()
                         .getMetadata()
                         .size());

        assertEquals(jsonObject.toString(),
                     response.get()
                         .getMetadata()
                         .get("someJsonObject")
                         .toString());

        assertEquals(jsonAsString,
                     response.get()
                         .getMetadata()
                         .get("someJsonObjectAsString")
                         .toString());

        List<MetadataEntity> entityList = metadataRepository.findByOwner_TenantIdAndOwner_TypeAndOwner_Id(tenantId,
                                                                                                          ownerType,
                                                                                                          UuidUtil.getUuidFromUrn(ownerUrn));

        assertFalse(entityList.isEmpty());

        assertFalse(entityList.isEmpty());
        assertEquals(2, entityList.size());
    }

    @Test
    public void testCreateWithComplexJsonMetadataValue3() throws Exception {

        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        final JSONObject jsonObject = new JSONObject("{\n"
                                                     + "    \"lastUpdatedAt\":\"2016-11-17T01:55:02.187Z\",\n"
                                                     + "    \"w\":6,\n"
                                                     + "    \"h\":6,\n"
                                                     + "    \"x\":6,\n"
                                                     + "    \"y\":0,\n"
                                                     + "    \"minW\":4,\n"
                                                     + "    \"minH\":4,\n"
                                                     + "    \"moved\":false,\n"
                                                     + "    \"static\":false,\n"
                                                     + "    \"state\": {\n"
                                                     + "        \"title\": \"asdasda\",\n"
                                                     + "         \"configuration\": {\n"
                                                     + "             \"metricType\": \"chart\"\n"
                                                     + "         }\n"
                                                     + "    }\n"
                                                     + "}");

        final String jsonAsString = "{\n"
                                    + "    \"lastUpdatedAt\":\"2016-11-17T01:55:02.187Z\",\n"
                                    + "    \"w\":6,\n"
                                    + "    \"h\":6,\n"
                                    + "    \"x\":6,\n"
                                    + "    \"y\":0,\n"
                                    + "    \"minW\":4,\n"
                                    + "    \"minH\":4,\n"
                                    + "    \"moved\":false,\n"
                                    + "    \"static\":false,\n"
                                    + "    \"state\": {\n"
                                    + "        \"title\": \"asdasda\",\n"
                                    + "         \"configuration\": {\n"
                                    + "             \"metricType\": \"chart\"\n"
                                    + "         }\n"
                                    + "    }\n"
                                    + "}";

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put("someJsonObjectAsString", jsonAsString);
        keyValues.put("someJsonObject", jsonObject);

        Optional<MetadataResponse> response = metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);

        Optional<MetadataValueResponse> getResponseJsonObject = metadataPersistenceService.findByKey(tenantUrn, ownerType, ownerUrn, "someJsonObject");
        Optional<MetadataValueResponse> getResponseJsonObjectAsString = metadataPersistenceService.findByKey(tenantUrn, ownerType, ownerUrn, "someJsonObjectAsString");

        assertTrue(response.isPresent());
        assertEquals(ownerType,
                     response.get()
                         .getOwnerType());
        assertEquals(ownerUrn,
                     response.get()
                         .getOwnerUrn());

        assertEquals(2,
                     response.get()
                         .getMetadata()
                         .size());

        assertEquals(jsonObject.toString(),
                     response.get()
                         .getMetadata()
                         .get("someJsonObject")
                         .toString());

        assertEquals(jsonAsString,
                     response.get()
                         .getMetadata()
                         .get("someJsonObjectAsString")
                         .toString());

        List<MetadataEntity> entityList = metadataRepository.findByOwner_TenantIdAndOwner_TypeAndOwner_Id(tenantId,
                                                                                                          ownerType,
                                                                                                          UuidUtil.getUuidFromUrn(ownerUrn));

        assertFalse(entityList.isEmpty());

        assertFalse(entityList.isEmpty());
        assertEquals(2, entityList.size());
    }

    @Test
    public void testCreateFailOnDuplicateKey() {


        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put("duplicateCreate", true);

        Optional<MetadataResponse> response1 = metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);
        assertTrue(response1.isPresent());

        Optional<MetadataResponse> response2 = metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);
        assertFalse(response2.isPresent());
    }

    @Test
    public void testVeryLongJsonObject() {

        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());


    }

    @Test
    public void testCreateFailOnEmptyMetadataMap() {

        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Map<String, Object> keyValues = new HashMap<>();

        Optional<MetadataResponse> response = metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);
        assertFalse(response.isPresent());
    }

    @Test
    public void testCreateFailOnNullMetadataMap() {

        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Map<String, Object> keyValues = null;

        Optional<MetadataResponse> response = metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);
        assertFalse(response.isPresent());
    }

    // endregion */

    // region Upsert

    @Test
    public void testUpsert() throws Exception {

        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        JSONObject o = new JSONObject("{\"x\":1,\"y\":2}");

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put("upsertBool", true);
        keyValues.put("upsertJson", o);
        keyValues.put("upsertNumber", 123);
        keyValues.put("upsertNull", null);
        keyValues.put("upsertString", "Text");

        Optional<MetadataResponse> response = metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);

        assertTrue(response.isPresent());
        assertEquals(ownerType,
                     response.get()
                         .getOwnerType());
        assertEquals(ownerUrn,
                     response.get()
                         .getOwnerUrn());
        assertEquals(5,
                     response.get()
                         .getMetadata()
                         .size());
        assertTrue(Boolean.parseBoolean(response.get()
                                            .getMetadata()
                                            .get("upsertBool")
                                            .toString()));
        assertEquals("Text",
                     response.get()
                         .getMetadata()
                         .get("upsertString")
                         .toString());

        ObjectNode output = (ObjectNode) response.get()
            .getMetadata()
            .get("upsertJson");
        assertEquals(1,
                     output.get("x")
                         .asInt());
        assertEquals(2,
                     output.get("y")
                         .asInt());

        List<MetadataEntity> entityList = metadataRepository.findByOwner_TenantIdAndOwner_TypeAndOwner_Id(tenantId,
                                                                                                          ownerType,
                                                                                                          UuidUtil.getUuidFromUrn(ownerUrn));

        assertFalse(entityList.isEmpty());
        assertEquals(5, entityList.size());
    }

    @Test
    public void testUpsertWorksOnDuplicateKey() {

        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put("duplicateUpsert", true);

        Optional<MetadataResponse> response1 = metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);
        assertTrue(response1.isPresent());

        assertTrue((Boolean) response1.get()
            .getMetadata()
            .get("duplicateUpsert"));

        keyValues.put("duplicateUpsert", false);

        Optional<MetadataResponse> response2 = metadataPersistenceService.upsert(tenantUrn, ownerType, ownerUrn, keyValues);
        assertTrue(response2.isPresent());

        assertFalse((Boolean) response2.get()
            .getMetadata()
            .get("duplicateUpsert"));
    }

    @Test
    public void testUpsertFailOnEmptyMetadataMap() {

        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Map<String, Object> keyValues = new HashMap<>();

        Optional<MetadataResponse> response = metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);
        assertFalse(response.isPresent());
    }

    @Test
    public void testUpsertFailOnNullMetadataMap() {

        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Map<String, Object> keyValues = null;

        Optional<MetadataResponse> response = metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);
        assertFalse(response.isPresent());
    }

    // endregion */

    // region Update

    @Test
    public void testUpdate() throws Exception {

        final String keyName = "updateMe";
        final Boolean value = true;
        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put(keyName, value);
        metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);

        Optional<MetadataValueResponse> o = metadataPersistenceService.findByKey(tenantUrn, ownerType, ownerUrn, keyName);

        assertTrue(o.isPresent());
        assertEquals(true,
                     o.get()
                         .getValue());
        assertEquals(tenantUrn,
                     o.get()
                         .getTenantUrn());

        Optional<MetadataResponse> response = metadataPersistenceService.update(tenantUrn, ownerType, ownerUrn, keyName, false);

        assertTrue(response.isPresent());
        assertEquals(ownerType,
                     response.get()
                         .getOwnerType());
        assertEquals(ownerUrn,
                     response.get()
                         .getOwnerUrn());
        assertEquals(1,
                     response.get()
                         .getMetadata()
                         .size());
        assertFalse(Boolean.parseBoolean(response.get()
                                             .getMetadata()
                                             .get("updateMe")
                                             .toString()));

        o = metadataPersistenceService.findByKey(tenantUrn, ownerType, ownerUrn, keyName);

        assertTrue(o.isPresent());
        assertEquals(false,
                     o.get()
                         .getValue());
        assertEquals(tenantUrn,
                     o.get()
                         .getTenantUrn());
    }

    // endregion */

    // region Delete

    @Test
    public void testDelete() {

        final String keyName = "deleteMe";
        final Boolean value = true;
        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put(keyName, value);

        metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);

        List<MetadataResponse> deleteList = metadataPersistenceService.delete(tenantUrn, ownerType, ownerUrn, keyName);

        assertFalse(deleteList.isEmpty());
        assertEquals(1, deleteList.size());
        assertEquals(ownerType,
                     deleteList.get(0)
                         .getOwnerType());
        assertEquals(ownerUrn,
                     deleteList.get(0)
                         .getOwnerUrn());
        assertEquals(1,
                     deleteList.get(0)
                         .getMetadata()
                         .size());
        assertTrue(Boolean.parseBoolean(deleteList.get(0)
                                            .getMetadata()
                                            .get(keyName)
                                            .toString()));
    }

    @Test
    public void testDeleteNonexistent() {

        final String keyName = "this-does-not-exist";
        final String ownerType = "Object";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        List<MetadataResponse> deleteList = metadataPersistenceService
            .delete(tenantUrn, ownerType, ownerUrn, keyName);

        assertTrue(deleteList.isEmpty());
    }

    // endregion */

    // region DeleteAllByOwner

    @Test
    public void testDeleteAllByOwner() {

        final Boolean value = true;
        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put("testDeleteAll1", value);
        keyValues.put("testDeleteAll2", value);

        metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);

        List<MetadataResponse> deleteList = metadataPersistenceService
            .deleteAllByOwner(tenantUrn, ownerType, ownerUrn);

        assertFalse(deleteList.isEmpty());
        assertEquals(2, deleteList.size());
        assertEquals(ownerType,
                     deleteList.get(0)
                         .getOwnerType());
        assertEquals(ownerUrn,
                     deleteList.get(0)
                         .getOwnerUrn());
        assertEquals(1,
                     deleteList.get(0)
                         .getMetadata()
                         .size());
    }

    @Test
    public void testDeleteAllByOwnerNonexistent() {

        final String keyName = "these-does-not-exist";
        final String ownerType = "Object";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        List<MetadataResponse> deleteList = metadataPersistenceService
            .deleteAllByOwner(tenantUrn, ownerType, ownerUrn);

        assertTrue(deleteList.isEmpty());
    }

    // endregion */

    // region Find by Key

    @Test
    public void testFindByKey() {

        final String keyName = "findMe";
        final Boolean value = true;
        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put(keyName, value);

        metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);

        Optional<MetadataValueResponse> response = metadataPersistenceService.findByKey(tenantUrn, ownerType, ownerUrn, keyName);

        assertTrue(response.isPresent());
        assertEquals(true,
                     response.get()
                         .getValue());
        assertEquals(tenantUrn,
                     response.get()
                         .getTenantUrn());
    }

    @Test
    public void testFindByKeyNonexistent() {

        final String keyName = "this-does-not-exist";
        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Optional<MetadataValueResponse> response = metadataPersistenceService.findByKey(tenantUrn, ownerType, ownerUrn, keyName);

        assertFalse(response.isPresent());
    }

    // endregion */

    // region Find by Key No Tenant

    @Test
    public void testFindByKeyNoTenant() {

        final String keyName = "findMeNoTenant";
        final Boolean value = true;
        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put(keyName, value);

        metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);

        Optional<MetadataValueResponse> response = metadataPersistenceService.findByKeyNoTenant(ownerType, ownerUrn, keyName);

        assertTrue(response.isPresent());
        assertEquals(true,
                     response.get()
                         .getValue());
        assertEquals(tenantUrn,
                     response.get()
                         .getTenantUrn());
    }

    @Test
    public void testFindByKeyNoTenantNonexistent() {

        final String keyName = "this-does-not-existNoTenant";
        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Optional<MetadataValueResponse> response = metadataPersistenceService.findByKeyNoTenant(ownerType, ownerUrn, keyName);

        assertFalse(response.isPresent());
    }

    // endregion */

    // region Find by Owner

    @Test
    public void testFindByOwner() {

        final Boolean value = true;
        final String key1 = "testFindByKey1";
        final String key2 = "testFindByKey2";
        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put(key1, value);
        keyValues.put(key2, value);

        metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);

        Collection<String> keySet = new ArrayList<>();
        keySet.add(key1);
        keySet.add(key2);

        Optional<MetadataResponse> response = metadataPersistenceService
            .findByOwner(tenantUrn, ownerType, ownerUrn, keySet);

        assertTrue(response.isPresent());

        assertEquals(ownerType,
                     response.get()
                         .getOwnerType());
        assertEquals(ownerUrn,
                     response.get()
                         .getOwnerUrn());
        assertEquals(keySet.size(),
                     response.get()
                         .getMetadata()
                         .size());
        assertTrue(Boolean.parseBoolean(response.get()
                                            .getMetadata()
                                            .get(key1)
                                            .toString()));
        assertTrue(Boolean.parseBoolean(response.get()
                                            .getMetadata()
                                            .get(key2)
                                            .toString()));
    }

    @Test
    public void testFindByOwnerWithoutKeys() {

        final Boolean value = true;
        final String key1 = "testFindWithoutKey1";
        final String key2 = "testFindWithoutKey2";
        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put(key1, value);
        keyValues.put(key2, value);

        metadataPersistenceService.create(tenantUrn, ownerType, ownerUrn, keyValues);

        Collection<String> keySet = new ArrayList<>();

        Optional<MetadataResponse> response = metadataPersistenceService
            .findByOwner(tenantUrn, ownerType, ownerUrn, keySet);

        assertTrue(response.isPresent());

        assertEquals(ownerType,
                     response.get()
                         .getOwnerType());
        assertEquals(ownerUrn,
                     response.get()
                         .getOwnerUrn());
        assertEquals(2,
                     response.get()
                         .getMetadata()
                         .size());
        assertTrue(Boolean.parseBoolean(response.get()
                                            .getMetadata()
                                            .get(key1)
                                            .toString()));
        assertTrue(Boolean.parseBoolean(response.get()
                                            .getMetadata()
                                            .get(key2)
                                            .toString()));
    }

    @Test
    public void testFindByOwnerNonexistent() {

        final String ownerType = "Thing";
        final String ownerUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());

        Collection<String> keySet = new ArrayList<>();

        Optional<MetadataResponse> response = metadataPersistenceService
            .findByOwner(tenantUrn, ownerType, ownerUrn, keySet);

        assertFalse(response.isPresent());
    }

    // endregion */

    // region findByOwnerType

    @Test
    public void testFindByTypePaging() throws Exception {

        populateData();

        int expectedPageSize = 3;
        int actualPageSize = 0;

        long expectedTotalSize = 12;
        long actualTotalSize = 0;

        Page<MetadataSingleResponse> response = metadataPersistenceService.findByOwnerType(tenantUrn, "someOwner", 1, 3);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertNotNull(response.getPage());

        actualPageSize = response.getData()
            .size();
        assertTrue("Expected " + expectedPageSize + " elements on page, but received " + actualPageSize, actualPageSize == expectedPageSize);

        actualTotalSize = response.getPage()
            .getTotalElements();
        assertTrue("Expected " + expectedTotalSize + " total elements, but received " + actualTotalSize, actualTotalSize == expectedTotalSize);
    }

    // endregion */

    // region Find By Key-Value Pairs

    @Test
    public void testFindByKeyValuePairs() throws Exception {

        populateData();

        final String[] ownerUrns = {
            "urn:thing:uuid:094bd91d-a093-4e40-b461-3b7ba7dc08bb",
            "urn:thing:uuid:06684869-f52d-4b59-a5fd-6424160fb48c",
            "urn:thing:uuid:89e0abc8-031f-4d89-8314-c8bf0a2b9913" };

        for (int i = 0; i < ownerUrns.length; i++) {
            createMetadataEntity("ownerType", ownerUrns[i], "fbK", 12);
            createMetadataEntity("ownerType", ownerUrns[i], "fbK4", 12);

            if (i > 0) {
                createMetadataEntity("ownerType", ownerUrns[i], "fbK2", "Test");
            }

            if (i > 1) {
                createMetadataEntity("ownerType", ownerUrns[i], "fbK3", 12);
            }
        }

        Map<String, Object> keyValuePairMap = new HashMap<>();
        keyValuePairMap.put("fbK", 12);
        keyValuePairMap.put("fbK3", 12);
        keyValuePairMap.put("fbK4", 12);
        keyValuePairMap.put("fbK2", "Test");

        Page<MetadataOwnerResponse> responsePage = metadataPersistenceService.findOwnersByTypeAndKeyValuePairs(tenantUrn, "ownerType",
                                                                                                               keyValuePairMap, 1, 10, null, null);

        assertEquals("urn:thing:uuid:89e0abc8-031f-4d89-8314-c8bf0a2b9913",
                     responsePage.getData()
                         .get(0)
                         .getOwnerUrn());

        assertEquals(1,
                     responsePage.getData()
                         .size());
        assertEquals(1,
                     responsePage.getPage()
                         .getSize());
        assertEquals(1,
                     responsePage.getPage()
                         .getTotalElements());

        assertEquals(1,
                     responsePage.getPage()
                         .getNumber());
        assertEquals(1,
                     responsePage.getPage()
                         .getTotalPages());
    }

    @Test
    public void testFindByKeyValuePairsDuplicateId() throws Exception {

        populateData();

        final String ownerUrn = "urn:thing:uuid:b5cb506a-698e-48c2-affa-80f391167990";

        createMetadataEntity("ownerA", ownerUrn, "key1", 12);
        createMetadataEntity("ownerA", ownerUrn, "key2", 12);
        createMetadataEntity("ownerB", ownerUrn, "key1", 12);
        createMetadataEntity("ownerB", ownerUrn, "key2", 12);

        Map<String, Object> keyValuePairMap = new HashMap<>();
        keyValuePairMap.put("key1", 12);
        keyValuePairMap.put("key2", 12);

        Page<MetadataOwnerResponse> responsePageA = metadataPersistenceService.findOwnersByTypeAndKeyValuePairs(tenantUrn, "ownerA",
                                                                                                                keyValuePairMap, 1, 10,
                                                                                                                SortOrder.ASC,
                                                                                                                "ownerType");

        assertEquals(1,
                     responsePageA.getData()
                         .size());
        assertEquals(1,
                     responsePageA.getPage()
                         .getSize());
        assertEquals(1,
                     responsePageA.getPage()
                         .getTotalElements());

        assertEquals(1,
                     responsePageA.getPage()
                         .getNumber());
        assertEquals(1,
                     responsePageA.getPage()
                         .getTotalPages());

        assertEquals(ownerUrn,
                     responsePageA.getData()
                         .get(0)
                         .getOwnerUrn());
        assertEquals("ownerA",
                     responsePageA.getData()
                         .get(0)
                         .getOwnerType());

        Page<MetadataOwnerResponse> responsePageB = metadataPersistenceService.findOwnersByTypeAndKeyValuePairs(tenantUrn, "ownerB",
                                                                                                                keyValuePairMap, 1, 10,
                                                                                                                SortOrder.ASC,
                                                                                                                "ownerType");

        assertEquals(1,
                     responsePageB.getData()
                         .size());
        assertEquals(1,
                     responsePageB.getPage()
                         .getSize());
        assertEquals(1,
                     responsePageB.getPage()
                         .getTotalElements());

        assertEquals(1,
                     responsePageB.getPage()
                         .getNumber());
        assertEquals(1,
                     responsePageB.getPage()
                         .getTotalPages());

        assertEquals(ownerUrn,
                     responsePageB.getData()
                         .get(0)
                         .getOwnerUrn());
        assertEquals("ownerB",
                     responsePageB.getData()
                         .get(0)
                         .getOwnerType());
    }

    @Test
    public void testFindByKeyValuePairsNonExistent() throws Exception {

        Map<String, Object> keyValuePairMap = new HashMap<>();
        keyValuePairMap.put("NoSuchKey", "NoSuchValue");
        keyValuePairMap.put("NoSuchKey2", "NoSuchValue2");

        Page<MetadataOwnerResponse> responsePage = metadataPersistenceService.findOwnersByTypeAndKeyValuePairs(tenantUrn,
                                                                                                               "someOwner",
                                                                                                               keyValuePairMap,
                                                                                                               1,
                                                                                                               10,
                                                                                                               SortOrder.ASC,
                                                                                                               "ownerType");

        assertTrue(responsePage.getData()
                       .isEmpty());

        assertEquals(0,
                     responsePage.getData()
                         .size());
        assertEquals(0,
                     responsePage.getPage()
                         .getSize());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalElements());

        assertEquals(0,
                     responsePage.getPage()
                         .getNumber());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalPages());
    }

    @Test
    public void testFindBySingleKeyValuePair() throws Exception {

        populateData();

        final String[] ownerUrns = {
            "urn:thing:uuid:4bb91563-ff39-486d-b542-5a91f6a3b884",
            "urn:thing:uuid:74fc2ae7-48c7-4775-a1e2-b785de9ad554" };

        for (String ownerUrn : ownerUrns) {
            createMetadataEntity("ownerType", ownerUrn, "single", "ABC");
        }

        Map<String, Object> keyValuePairMap = new HashMap<>();
        keyValuePairMap.put("single", "ABC");

        Page<MetadataOwnerResponse> responsePage = metadataPersistenceService.findOwnersByTypeAndKeyValuePairs(tenantUrn,
                                                                                                               "ownerType",
                                                                                                               keyValuePairMap,
                                                                                                               1,
                                                                                                               10,
                                                                                                               null,
                                                                                                               null);

        assertEquals(ownerUrns[0],
                     responsePage.getData()
                         .get(0)
                         .getOwnerUrn());
        assertEquals(ownerUrns[1],
                     responsePage.getData()
                         .get(1)
                         .getOwnerUrn());

        assertEquals(2,
                     responsePage.getData()
                         .size());
        assertEquals(2,
                     responsePage.getPage()
                         .getSize());
        assertEquals(2,
                     responsePage.getPage()
                         .getTotalElements());

        assertEquals(1,
                     responsePage.getPage()
                         .getNumber());
        assertEquals(1,
                     responsePage.getPage()
                         .getTotalPages());
    }

    @Test
    public void testFindBySingleKeyValuePairNonexistent() throws Exception {

        Map<String, Object> keyValuePairMap = new HashMap<>();
        keyValuePairMap.put("NoSuchKey", "NoSuchValue");

        Page<MetadataOwnerResponse> responsePage = metadataPersistenceService.findOwnersByTypeAndKeyValuePairs(tenantUrn,
                                                                                                               "someOwner",
                                                                                                               keyValuePairMap,
                                                                                                               1,
                                                                                                               10,
                                                                                                               null,
                                                                                                               null);

        assertTrue(responsePage.getData()
                       .isEmpty());

        assertEquals(0,
                     responsePage.getData()
                         .size());
        assertEquals(0,
                     responsePage.getPage()
                         .getSize());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalElements());

        assertEquals(0,
                     responsePage.getPage()
                         .getNumber());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalPages());
    }

    // endregion

    // region Find By Key-Value Pairs no tenant

    @Test
    public void testFindByKeyValuePairsNoTenant() throws Exception {

        populateData();

        final String[] ownerUrns = {
            "urn:thing:uuid:39e2fae3-e459-4ab8-b051-9ae80cbdd8ec",
            "urn:thing:uuid:34eb37d3-6528-4d7f-9629-730705819b35" };

        createMetadataEntityExplicitTenant(tenantId, "ownerType", ownerUrns[0], "keyA", 1);
        createMetadataEntityExplicitTenant(tenantId, "ownerType", ownerUrns[0], "keyB", 2);

        createMetadataEntityExplicitTenant(tenantId2, "ownerType", ownerUrns[1], "keyA", 1);
        createMetadataEntityExplicitTenant(tenantId2, "ownerType", ownerUrns[1], "keyB", 2);

        Map<String, Object> keyValuePairMap = new HashMap<>();
        keyValuePairMap.put("keyA", 1);
        keyValuePairMap.put("keyB", 2);

        Page<MetadataOwnerResponse> responsePage = metadataPersistenceService.findOwnersByTypeAndKeyValuePairsNoTenant("ownerType",
                                                                                                                       keyValuePairMap,
                                                                                                                       1,
                                                                                                                       10,
                                                                                                                       null,
                                                                                                                       null);

        assertEquals(2,
                     responsePage.getData()
                         .size());
        assertEquals(2,
                     responsePage.getPage()
                         .getSize());
        assertEquals(2,
                     responsePage.getPage()
                         .getTotalElements());

        assertEquals(1,
                     responsePage.getPage()
                         .getNumber());
        assertEquals(1,
                     responsePage.getPage()
                         .getTotalPages());
    }

    @Test
    public void testFindByKeyValuePairsDuplicateIdNoTenant() throws Exception {

        populateData();

        final String ownerUrn = "urn:thing:uuid:b5cb506a-698e-48c2-affa-80f391167990";

        createMetadataEntity("ownerA", ownerUrn, "key1", 12);
        createMetadataEntity("ownerA", ownerUrn, "key2", 12);
        createMetadataEntity("ownerB", ownerUrn, "key1", 12);
        createMetadataEntity("ownerB", ownerUrn, "key2", 12);

        Map<String, Object> keyValuePairMap = new HashMap<>();
        keyValuePairMap.put("key1", 12);
        keyValuePairMap.put("key2", 12);

        Page<MetadataOwnerResponse> responsePageA = metadataPersistenceService.findOwnersByTypeAndKeyValuePairs(tenantUrn, "ownerA",
                                                                                                                keyValuePairMap, 1, 10,
                                                                                                                SortOrder.ASC,
                                                                                                                "ownerType");

        assertEquals(1,
                     responsePageA.getData()
                         .size());
        assertEquals(1,
                     responsePageA.getPage()
                         .getSize());
        assertEquals(1,
                     responsePageA.getPage()
                         .getTotalElements());

        assertEquals(1,
                     responsePageA.getPage()
                         .getNumber());
        assertEquals(1,
                     responsePageA.getPage()
                         .getTotalPages());

        assertEquals(ownerUrn,
                     responsePageA.getData()
                         .get(0)
                         .getOwnerUrn());
        assertEquals("ownerA",
                     responsePageA.getData()
                         .get(0)
                         .getOwnerType());

        Page<MetadataOwnerResponse> responsePageB = metadataPersistenceService.findOwnersByTypeAndKeyValuePairs(tenantUrn, "ownerB",
                                                                                                                keyValuePairMap, 1, 10,
                                                                                                                SortOrder.ASC,
                                                                                                                "ownerType");

        assertEquals(1,
                     responsePageB.getData()
                         .size());
        assertEquals(1,
                     responsePageB.getPage()
                         .getSize());
        assertEquals(1,
                     responsePageB.getPage()
                         .getTotalElements());

        assertEquals(1,
                     responsePageB.getPage()
                         .getNumber());
        assertEquals(1,
                     responsePageB.getPage()
                         .getTotalPages());

        assertEquals(ownerUrn,
                     responsePageB.getData()
                         .get(0)
                         .getOwnerUrn());
        assertEquals("ownerB",
                     responsePageB.getData()
                         .get(0)
                         .getOwnerType());
    }

    @Test
    public void testFindByKeyValuePairsNonExistentNoTenant() throws Exception {

        Map<String, Object> keyValuePairMap = new HashMap<>();
        keyValuePairMap.put("NoSuchKey", "NoSuchValue");
        keyValuePairMap.put("NoSuchKey2", "NoSuchValue2");

        Page<MetadataOwnerResponse> responsePage = metadataPersistenceService.findOwnersByTypeAndKeyValuePairsNoTenant("someOwner",
                                                                                                                       keyValuePairMap,
                                                                                                                       1,
                                                                                                                       10,
                                                                                                                       SortOrder.ASC,
                                                                                                                       "ownerType");

        assertTrue(responsePage.getData()
                       .isEmpty());

        assertEquals(0,
                     responsePage.getData()
                         .size());
        assertEquals(0,
                     responsePage.getPage()
                         .getSize());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalElements());

        assertEquals(0,
                     responsePage.getPage()
                         .getNumber());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalPages());
    }

    @Test
    public void testFindBySingleKeyValuePairNoTenant() throws Exception {

        populateData();

        final String[] ownerUrns = {
            "urn:thing:uuid:4bb91563-ff39-486d-b542-5a91f6a3b884",
            "urn:thing:uuid:74fc2ae7-48c7-4775-a1e2-b785de9ad554" };

        for (String ownerUrn : ownerUrns) {
            createMetadataEntity("ownerType", ownerUrn, "single", "ABC");
        }

        Map<String, Object> keyValuePairMap = new HashMap<>();
        keyValuePairMap.put("single", "ABC");

        Page<MetadataOwnerResponse> responsePage = metadataPersistenceService.findOwnersByTypeAndKeyValuePairsNoTenant("ownerType",
                                                                                                                       keyValuePairMap,
                                                                                                                       1,
                                                                                                                       10,
                                                                                                                       null,
                                                                                                                       null);

        assertEquals(ownerUrns[0],
                     responsePage.getData()
                         .get(0)
                         .getOwnerUrn());
        assertEquals(ownerUrns[1],
                     responsePage.getData()
                         .get(1)
                         .getOwnerUrn());

        assertEquals(2,
                     responsePage.getData()
                         .size());
        assertEquals(2,
                     responsePage.getPage()
                         .getSize());
        assertEquals(2,
                     responsePage.getPage()
                         .getTotalElements());

        assertEquals(1,
                     responsePage.getPage()
                         .getNumber());
        assertEquals(1,
                     responsePage.getPage()
                         .getTotalPages());
    }

    @Test
    public void testFindBySingleKeyValuePairNonexistentNoTenant() throws Exception {

        Map<String, Object> keyValuePairMap = new HashMap<>();
        keyValuePairMap.put("NoSuchKey", "NoSuchValue");

        Page<MetadataOwnerResponse> responsePage = metadataPersistenceService.findOwnersByTypeAndKeyValuePairsNoTenant("ownerType",
                                                                                                                       keyValuePairMap,
                                                                                                                       1,
                                                                                                                       10,
                                                                                                                       null,
                                                                                                                       null);

        assertTrue(responsePage.getData()
                       .isEmpty());

        assertEquals(0,
                     responsePage.getData()
                         .size());
        assertEquals(0,
                     responsePage.getPage()
                         .getSize());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalElements());

        assertEquals(0,
                     responsePage.getPage()
                         .getNumber());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalPages());
    }

    // endregion

    // region populateData
    private void populateData() throws Exception {

        int i = 0;
        for (String urn : urns) {

            MetadataOwnerEntity owner = MetadataOwnerEntity.builder()
                .tenantId(tenantId)
                .type("someOwner")
                .id(UuidUtil.getUuidFromUrn(urn))
                .build();

            MetadataEntity entity = MetadataEntity.builder()
                .owner(owner)
                .keyName("someName")
                .dataType(MetadataDataType.INTEGER)
                .value(String.format("%d", i++))
                .build();

            metadataRepository.save(entity);
        }
    }

    private void createMetadataEntity(String ownerType, String ownerUrn, String key, Object value) throws Exception {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(key, value);

        Assert.assertTrue(metadataPersistenceService.create(UuidUtil.getTenantUrnFromUuid(tenantId), ownerType, ownerUrn, metadata)
                              .isPresent());
    }

    private void createMetadataEntityExplicitTenant(UUID tenantId, String ownerType, String ownerUrn, String key, Object value) throws Exception {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(key, value);

        Assert.assertTrue(metadataPersistenceService.create(UuidUtil.getTenantUrnFromUuid(tenantId), ownerType, ownerUrn, metadata)
                              .isPresent());
    }

    // endregion */
}
