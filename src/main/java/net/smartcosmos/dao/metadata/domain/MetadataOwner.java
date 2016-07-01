package net.smartcosmos.dao.metadata.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class MetadataOwner {
    private String ownerType;
    private UUID ownerId;
    private UUID tenantId;
}
