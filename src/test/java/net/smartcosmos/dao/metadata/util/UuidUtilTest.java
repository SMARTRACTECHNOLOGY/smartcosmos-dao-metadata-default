package net.smartcosmos.dao.metadata.util;

import java.util.UUID;

import org.junit.*;

import static org.junit.Assert.*;

public class UuidUtilTest {

    @Test
    public void getUuidFromUrn() throws Exception {

        final String expectedUuid = "8e24eabd-1be9-46ac-8c7d-1e753746b413";
        final String urn = "urn:thing:uuid:" + expectedUuid;

        UUID uuid = UuidUtil.getUuidFromUrn(urn);

        assertEquals(expectedUuid, uuid.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getUuidFromInvaldiUrn() throws Exception {

        final String urn = "this-is-no-valid-urn";
        UuidUtil.getUuidFromUrn(urn);
    }

    @Test
    public void getThingUrnFromUuid() throws Exception {

        final String uuid = "8e24eabd-1be9-46ac-8c7d-1e753746b413";
        final String expectedUrn = "urn:thing:uuid:" + uuid;

        String urn = UuidUtil.getThingUrnFromUuid(UUID.fromString(uuid));

        assertEquals(expectedUrn, urn);
    }

    @Test
    public void getTenantUrnFromUuid() throws Exception {

        final String uuid = "8e24eabd-1be9-46ac-8c7d-1e753746b413";
        final String expectedUrn = "urn:tenant:uuid:" + uuid;

        String urn = UuidUtil.getTenantUrnFromUuid(UUID.fromString(uuid));

        assertEquals(expectedUrn, urn);
    }

    @Test
    public void getPrefixUrnFromUuid() throws Exception {

        final String uuid = "8e24eabd-1be9-46ac-8c7d-1e753746b413";
        final String expectedUrn = "urn:prefix:uuid:" + uuid;

        String urn = UuidUtil.getPrefixUrnFromUuid("prefix", UUID.fromString(uuid));

        assertEquals(expectedUrn, urn);
    }
}
