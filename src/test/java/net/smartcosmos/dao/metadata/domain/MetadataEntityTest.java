package net.smartcosmos.dao.metadata.domain;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.*;

@SuppressWarnings("Duplicates")
public class MetadataEntityTest {

    private static Validator validator;

    private static final MetadataDataType DATA_TYPE = MetadataDataType.STRING;
    private static final MetadataOwnerEntity OWNER = MetadataOwnerEntity.builder().build();
    private static final String KEY_NAME = RandomStringUtils.randomAlphanumeric(255);
    private static final String KEY_NAME_INVALID = RandomStringUtils.randomAlphanumeric(256);
    private static final String VALUE = RandomStringUtils.randomAlphanumeric(767);
    private static final String VALUE_INVALID = RandomStringUtils.randomAlphanumeric(768);

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void thatEverythingIsOk() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .owner(OWNER)
            .dataType(DATA_TYPE)
            .keyName(KEY_NAME)
            .value(VALUE)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertTrue(violationSet.isEmpty());
    }

    // region Data Type

    @Test
    public void thatDataTypeIsNotNull() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .owner(OWNER)
//            .dataType(DATA_TYPE)
            .keyName(KEY_NAME)
            .value(VALUE)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.NotNull.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("dataType", violationSet.iterator().next().getPropertyPath().toString());
    }

    // endregion

    // region Key

    @Test
    public void thatKeyIsNotNull() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .owner(OWNER)
            .dataType(DATA_TYPE)
//            .keyName(KEY)
            .value(VALUE)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("keyName", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatKeyIsNotEmpty() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .owner(OWNER)
            .dataType(DATA_TYPE)
            .keyName("")
            .value(VALUE)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("keyName", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatKeyInvalidFails() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .owner(OWNER)
            .dataType(DATA_TYPE)
            .keyName(KEY_NAME_INVALID)
            .value(VALUE)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.Size.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("keyName", violationSet.iterator().next().getPropertyPath().toString());
    }

    // endregion

    // region Value

    @Test
    public void thatValueAllowsNull() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .owner(OWNER)
            .dataType(DATA_TYPE)
            .keyName(KEY_NAME)
//            .value(VALUE)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertTrue(violationSet.isEmpty());
    }

    @Test
    public void thatValueAllowsEmpty() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .owner(OWNER)
            .dataType(DATA_TYPE)
            .keyName(KEY_NAME)
            .value("")
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertTrue(violationSet.isEmpty());
    }

    @Test
    public void thatValueInvalidFails() {

        MetadataEntity metadataEntity = MetadataEntity.builder()
            .owner(OWNER)
            .dataType(DATA_TYPE)
            .keyName(KEY_NAME)
            .value(VALUE_INVALID)
            .build();

        Set<ConstraintViolation<MetadataEntity>> violationSet = validator.validate(metadataEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.Size.message}", violationSet.iterator().next().getMessageTemplate());
        assertEquals("value", violationSet.iterator().next().getPropertyPath().toString());
    }

    // endregion
}
