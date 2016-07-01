package net.smartcosmos.dao.metadata.domain;

import java.util.UUID;

public interface MetadataOwnerProjection {

    String getOwnerType();

    UUID getOwnerId();

    UUID getTenantId();
}
