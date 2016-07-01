package net.smartcosmos.dao.metadata.domain;

import java.util.UUID;

public interface MetadataOwnerProjection {

    UUID getOwnerId();

    String getOwnerType();

    UUID getTenantId();
}
