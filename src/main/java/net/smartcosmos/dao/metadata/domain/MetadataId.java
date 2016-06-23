package net.smartcosmos.dao.metadata.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
public class MetadataId implements Serializable {

    private UUID ownerId;

    private String ownerType;

    private String keyName;

    private UUID tenantId;
}
