package net.smartcosmos.dao.metadata.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Entity(name = "metadata")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Data
@EntityListeners({ AuditingEntityListener.class })
@Table(name = "metadata", uniqueConstraints = @UniqueConstraint(
    columnNames = { "id", "tenantId"}) )
public class MetadataEntity {

    private static final int UUID_LENGTH = 16;
    private static final int KEY_NAME_LENGTH = 255;
    private static final int DATA_TYPE_LENGTH = 255;
    private static final int OWNER_TYPE_LENGTH = 255;
    private static final int VALUE_LENGTH = 767;

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-binary")
    @Column(name = "id", length = UUID_LENGTH)
    private UUID id;

    @NotEmpty
    @Size(max = OWNER_TYPE_LENGTH)
    @Column(name = "ownerType", length = OWNER_TYPE_LENGTH, nullable = false, updatable = false)
    private String ownerType;

    @NotNull
    @Type(type = "uuid-binary")
    @Column(name = "ownerId", length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID ownerId;

    @NotEmpty
    @Size(max = DATA_TYPE_LENGTH)
    @Column(name = "dataType", length = DATA_TYPE_LENGTH, nullable = false, updatable = true)
    private String dataType;

    @NotEmpty
    @Size(max = KEY_NAME_LENGTH)
    @Column(name = "keyName", length = KEY_NAME_LENGTH, nullable = false, updatable = false)
    private String keyName;

    @NotEmpty
    @Size(max = VALUE_LENGTH)
    @Column(name = "value", length = VALUE_LENGTH, nullable = false, updatable = true)
    private String value;

    @NotNull
    @Type(type = "uuid-binary")
    @Column(name = "tenantId", length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID tenantId;

    @CreatedDate
    @Column(name = "createdTimestamp", insertable = true, updatable = false)
    private Long created;

    @LastModifiedDate
    @Column(name = "lastModifiedTimestamp", nullable = false, insertable = true, updatable = true)
    private Long lastModified;
}
