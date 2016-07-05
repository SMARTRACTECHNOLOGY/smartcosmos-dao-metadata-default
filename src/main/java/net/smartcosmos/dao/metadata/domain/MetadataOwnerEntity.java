package net.smartcosmos.dao.metadata.domain;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

@Entity(name = "metadataOwner")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Data
@Table(name = "metadataOwner")
public class MetadataOwnerEntity {

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
}
