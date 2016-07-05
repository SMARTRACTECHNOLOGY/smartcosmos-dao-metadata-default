package net.smartcosmos.dao.metadata.domain;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MetadataId implements Serializable {

    private MetadataOwnerEntity owner;

    private String keyName;
}
