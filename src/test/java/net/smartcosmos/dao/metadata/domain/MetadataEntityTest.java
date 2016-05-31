package net.smartcosmos.dao.metadata.domain;

import net.smartcosmos.util.UuidUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class MetadataEntityTest {

    private static Validator validator;

    private static final UUID ID = UuidUtil.getNewUuid();
    private static final UUID ACCOUNT_ID = UuidUtil.getNewUuid();
    private static final String DATA_TYPE = "StringType";
    private static final String DATA_TYPE_INVALID = RandomStringUtils.randomAlphanumeric(256);
    private static final String ENTITY_REFERENCE_TYPE = "Object";
    private static final String ENTITY_REFERENCE_TYPE_INVALID = RandomStringUtils.randomAlphanumeric(256);
    private static final UUID REFERENCE_ID = UuidUtil.getNewUuid();
    private static final String KEY = RandomStringUtils.randomAlphanumeric(255);
    private static final String KEY_INVALID = RandomStringUtils.randomAlphanumeric(256);
    private static final String RAW_VALUE = RandomStringUtils.randomAlphanumeric(767);
    private static final String RAW_VALUE_INVALID = RandomStringUtils.randomAlphanumeric(768);
    private static final String MONIKER = RandomStringUtils.randomAlphanumeric(2048);
    private static final String MONIKER_INVALID = RandomStringUtils.randomAlphanumeric(2049);

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void thatEverythingIsOk() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID)
            .key(KEY)
            .rawValue(RAW_VALUE)
            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertTrue(violationSet.isEmpty());
    }

    @Test
    public void thatAccountIdIsNotNull() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
//            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID)
            .key(KEY)
            .rawValue(RAW_VALUE)
            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.NotNull.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("accountId", violationSet.iterator().next().getPropertyPath().toString());
    }

    // region Data Type

    @Test
    public void thatDataTypeIsNotNull() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
//            .dataType(DATA_TYPE)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID)
            .key(KEY)
            .rawValue(RAW_VALUE)
            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("dataType", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatDataTypeIsNotEmpty() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType("")
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID)
            .key(KEY)
            .rawValue(RAW_VALUE)
            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("dataType", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatDataTypeInvalidFails() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE_INVALID)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID)
            .key(KEY)
            .rawValue(RAW_VALUE)
            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.Size.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("dataType", violationSet.iterator().next().getPropertyPath().toString());
    }

    // endregion

    // region Entity Reference Type

    @Test
    public void thatEntityReferenceTypeIsNotNull() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE)
//            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID)
            .key(KEY)
            .rawValue(RAW_VALUE)
            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("entityReferenceType", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatEntityReferenceTypeIsNotEmpty() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE)
            .entityReferenceType("")
            .referenceId(REFERENCE_ID)
            .key(KEY)
            .rawValue(RAW_VALUE)
            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("entityReferenceType", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatEntityReferenceTypeInvalidFails() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE)
            .entityReferenceType(ENTITY_REFERENCE_TYPE_INVALID)
            .referenceId(REFERENCE_ID)
            .key(KEY)
            .rawValue(RAW_VALUE)
            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.Size.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("entityReferenceType", violationSet.iterator().next().getPropertyPath().toString());
    }

    // endregion

    // region Reference ID

    @Test
    public void thatReferenceIdIsNotNull() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
//            .referenceId(REFERENCE_ID)
            .key(KEY)
            .rawValue(RAW_VALUE)
            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.NotNull.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("referenceId", violationSet.iterator().next().getPropertyPath().toString());
    }

    // endregion

    // region Key

    @Test
    public void thatKeyIsNotNull() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID)
//            .key(KEY)
            .rawValue(RAW_VALUE)
            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("key", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatKeyIsNotEmpty() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID)
            .key("")
            .rawValue(RAW_VALUE)
            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("key", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatKeyInvalidFails() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID)
            .key(KEY_INVALID)
            .rawValue(RAW_VALUE)
            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.Size.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("key", violationSet.iterator().next().getPropertyPath().toString());
    }

    // endregion

    // region Raw Value

    @Test
    public void thatRawValueIsNotNull() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID)
            .key(KEY)
//            .rawValue(RAW_VALUE)
            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("rawValue", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatRawValueIsNotEmpty() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID)
            .key(KEY)
            .rawValue("")
            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("rawValue", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatRawValueInvalidFails() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID)
            .key(KEY)
            .rawValue(RAW_VALUE_INVALID)
            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.Size.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("rawValue", violationSet.iterator().next().getPropertyPath().toString());
    }

    // endregion

    // region Moniker

    @Test
    public void thatMonikerMayBeNull() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID)
            .key(KEY)
            .rawValue(RAW_VALUE)
//            .moniker(MONIKER)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertTrue(violationSet.isEmpty());
    }

    @Test
    public void thatMonikerMayBeEmpty() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID)
            .key(KEY)
            .rawValue(RAW_VALUE)
            .moniker("")
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertTrue(violationSet.isEmpty());
    }

    @Test
    public void thatMonikerInvalidFails() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .id(ID)
            .accountId(ACCOUNT_ID)
            .dataType(DATA_TYPE)
            .entityReferenceType(ENTITY_REFERENCE_TYPE)
            .referenceId(REFERENCE_ID)
            .key(KEY)
            .rawValue(RAW_VALUE)
            .moniker(MONIKER_INVALID)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.Size.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("moniker", violationSet.iterator().next().getPropertyPath().toString());
    }

    // endregion
}
