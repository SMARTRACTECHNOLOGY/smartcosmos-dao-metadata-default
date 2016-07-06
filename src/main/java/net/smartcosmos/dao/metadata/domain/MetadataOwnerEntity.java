package net.smartcosmos.dao.metadata.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

@Entity(name = "metadataOwner")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(exclude = {"metadataEntities"})
@ToString(exclude = {"metadataEntities"})
@Table(
    name = "metadataOwner",
    uniqueConstraints = {@UniqueConstraint(columnNames = { "type", "id", "tenantId" })}
    )
public class MetadataOwnerEntity implements Serializable {

    public static final String ID_FIELD_NAME = "internalId";
    public static final String OWNER_TYPE_FIELD_NAME = "type";
    public static final String OWNER_ID_FIELD_NAME = "id";
    public static final String TENANT_ID_FIELD_NAME = "tenantId";

    private static final int UUID_LENGTH = 16;
    private static final int OWNER_TYPE_LENGTH = 255;

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-binary")
    @Column(name = ID_FIELD_NAME, length = UUID_LENGTH)
    private UUID internalId;

    @NotEmpty
    @Size(max = OWNER_TYPE_LENGTH)
    @Column(name = OWNER_TYPE_FIELD_NAME, length = OWNER_TYPE_LENGTH, nullable = false, updatable = false)
    private String type;

    @NotNull
    @Type(type = "uuid-binary")
    @Column(name = OWNER_ID_FIELD_NAME, length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Type(type = "uuid-binary")
    @Column(name = TENANT_ID_FIELD_NAME, length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID tenantId;

    @OneToMany(mappedBy = MetadataEntity.OWNER_FIELD_NAME, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @MapKey(name = MetadataEntity.KEY_NAME_FIELD_NAME)
    private Map<String, MetadataEntity> metadataEntities = new HashMap<>();

    @Builder
    @java.beans.ConstructorProperties({ "internalId", "type", "id", "tenantId", "metadataEntities" })
    public MetadataOwnerEntity(UUID internalId, String type, UUID id, UUID tenantId, Set<MetadataEntity> metadataEntities) {
        this.internalId = internalId;
        this.type = type;
        this.id = id;
        this.tenantId = tenantId;
        this.metadataEntities = new HashMap<>();
    }

    public void addMetadataEntity(MetadataEntity metadataEntity) {
        metadataEntity.setOwner(this);
        metadataEntities.put(metadataEntity.getKeyName(), metadataEntity);
    }
}
