package net.smartcosmos.dao.metadata.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "metadata")
@IdClass(MetadataId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Data
@EntityListeners({ AuditingEntityListener.class })
@Table(name = "metadata", uniqueConstraints = @UniqueConstraint(
    columnNames = { "ownerId", "tenantId", "dataType", "keyName"}) )
public class MetadataEntity implements Serializable {

    private static final int UUID_LENGTH = 16;
    private static final int KEY_NAME_LENGTH = 255;
    private static final int DATA_TYPE_LENGTH = 255;
    private static final int OWNER_TYPE_LENGTH = 255;
    private static final int VALUE_LENGTH = 767;

    @Id
    @NotEmpty
    @Size(max = OWNER_TYPE_LENGTH)
    @Column(name = "ownerType", length = OWNER_TYPE_LENGTH, nullable = false, updatable = false)
    private String ownerType;

    @Id
    @NotNull
    @Type(type = "uuid-binary")
    @Column(name = "ownerId", length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID ownerId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "dataType", length = DATA_TYPE_LENGTH, nullable = false, updatable = true)
    private MetadataDataType dataType;

    @Id
    @NotEmpty
    @Size(max = KEY_NAME_LENGTH)
    @Column(name = "keyName", length = KEY_NAME_LENGTH, nullable = false, updatable = false)
    private String keyName;

    @Size(max = VALUE_LENGTH)
    @Column(name = "value", length = VALUE_LENGTH, nullable = true, updatable = true)
    private String value;

    @Id
    @NotNull
    @Type(type = "uuid-binary")
    @Column(name = "tenantId", length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID tenantId;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created", insertable = true, updatable = false)
    private Date created;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastModified", nullable = false, insertable = true, updatable = true)
    private Date lastModified;
}
