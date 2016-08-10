package net.smartcosmos.dao.metadata.domain;

import java.util.Set;
import java.util.UUID;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.*;

import net.smartcosmos.util.UuidUtil;

import static org.junit.Assert.*;

public class MetadataOwnerEntityTest {

    private static Validator validator;

    private static final UUID TENANT_ID = UuidUtil.getNewUuid();
    private static final String OWNER_TYPE = RandomStringUtils.randomAlphanumeric(255);
    private static final String OWNER_TYPE_INVALID = RandomStringUtils.randomAlphanumeric(256);
    private static final UUID OWNER_ID = UuidUtil.getNewUuid();

    @BeforeClass
    public static void setUp() {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void thatTenantIdIsNotNull() {

        MetadataOwnerEntity owner = MetadataOwnerEntity.builder()
            .type(OWNER_TYPE)
            .id(OWNER_ID)
            //            .tenantId(TENANT_ID)
            .build();

        Set<ConstraintViolation<MetadataOwnerEntity>> violationSet = validator.validate(owner);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.NotNull.message}",
                     violationSet.iterator()
                         .next()
                         .getMessageTemplate());
        assertEquals("tenantId",
                     violationSet.iterator()
                         .next()
                         .getPropertyPath()
                         .toString());
    }

    // region Owner Type

    @Test
    public void thatOwnerTypeIsNotNull() {

        MetadataOwnerEntity owner = MetadataOwnerEntity.builder()
            //            .type(OWNER)
            .id(OWNER_ID)
            .tenantId(TENANT_ID)
            .build();

        Set<ConstraintViolation<MetadataOwnerEntity>> violationSet = validator.validate(owner);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}",
                     violationSet.iterator()
                         .next()
                         .getMessageTemplate());
        assertEquals("type",
                     violationSet.iterator()
                         .next()
                         .getPropertyPath()
                         .toString());
    }

    @Test
    public void thatOwnerTypeIsNotEmpty() {

        MetadataOwnerEntity owner = MetadataOwnerEntity.builder()
            .tenantId(TENANT_ID)
            .type("")
            .id(OWNER_ID)
            .build();

        Set<ConstraintViolation<MetadataOwnerEntity>> violationSet = validator.validate(owner);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}",
                     violationSet.iterator()
                         .next()
                         .getMessageTemplate());
        assertEquals("type",
                     violationSet.iterator()
                         .next()
                         .getPropertyPath()
                         .toString());
    }

    @Test
    public void thatOwnerTypeInvalidFails() {

        MetadataOwnerEntity owner = MetadataOwnerEntity.builder()
            .tenantId(TENANT_ID)
            .type(OWNER_TYPE_INVALID)
            .id(OWNER_ID)
            .build();

        Set<ConstraintViolation<MetadataOwnerEntity>> violationSet = validator.validate(owner);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.Size.message}",
                     violationSet.iterator()
                         .next()
                         .getMessageTemplate());
        assertEquals("type",
                     violationSet.iterator()
                         .next()
                         .getPropertyPath()
                         .toString());
    }

    // endregion

    // region Owner ID

    @Test
    public void thatOwnerIdIsNotNull() {

        MetadataOwnerEntity owner = MetadataOwnerEntity.builder()
            .tenantId(TENANT_ID)
            .type(OWNER_TYPE)
            //            .id(REFERENCE_ID)
            .build();

        Set<ConstraintViolation<MetadataOwnerEntity>> violationSet = validator.validate(owner);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.NotNull.message}",
                     violationSet.iterator()
                         .next()
                         .getMessageTemplate());
        assertEquals("id",
                     violationSet.iterator()
                         .next()
                         .getPropertyPath()
                         .toString());
    }

    // endregion
}
