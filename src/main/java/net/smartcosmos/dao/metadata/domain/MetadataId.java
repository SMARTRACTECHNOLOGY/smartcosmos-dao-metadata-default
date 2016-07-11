package net.smartcosmos.dao.metadata.domain;

import java.io.Serializable;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MetadataId implements Serializable {

    private UUID owner;

    private String keyName;
}
