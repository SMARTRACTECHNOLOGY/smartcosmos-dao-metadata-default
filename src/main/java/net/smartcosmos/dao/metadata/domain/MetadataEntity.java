package net.smartcosmos.dao.metadata.domain;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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

import net.smartcosmos.dao.metadata.converter.attribute.MetadataDataTypeConverter;

@Entity(name = "metadata")
@IdClass(MetadataId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Data
@EntityListeners({ AuditingEntityListener.class })
@Table(name = "metadata")
public class MetadataEntity implements Serializable {

    public static final String OWNER_FIELD_NAME = "owner";

    public static final String OWNER_TYPE_FIELD_NAME = "ownerType";
    public static final String OWNER_ID_FIELD_NAME = "ownerId";
    public static final String DATA_TYPE_FIELD_NAME = "dataType";
    public static final String KEY_NAME_FIELD_NAME = "keyName";
    public static final String VALUE_FIELD_NAME = "value";
    public static final String TENANT_ID_FIELD_NAME = "tenantId";
    public static final String CREATED_FIELD_NAME = "created";
    public static final String LAST_MODIFIED_FIELD_NAME = "lastModified";

    private static final int UUID_LENGTH = 16;
    private static final int KEY_NAME_LENGTH = 255;
    private static final int DATA_TYPE_LENGTH = 255;
    private static final int OWNER_TYPE_LENGTH = 255;
    private static final int VALUE_LENGTH = 767;

    @Id
    @NotNull
    @Type(type = "uuid-binary")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private MetadataOwnerEntity owner;

    @NotNull
    @Convert(converter = MetadataDataTypeConverter.class)
    @Column(name = DATA_TYPE_FIELD_NAME, nullable = false, updatable = true)
    private MetadataDataType dataType;

    @Id
    @NotEmpty
    @Size(max = KEY_NAME_LENGTH)
    @Column(name = KEY_NAME_FIELD_NAME, length = KEY_NAME_LENGTH, nullable = false, updatable = false)
    private String keyName;

    @Size(max = VALUE_LENGTH)
    @Column(name = VALUE_FIELD_NAME, length = VALUE_LENGTH, nullable = true, updatable = true)
    private String value;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = CREATED_FIELD_NAME, nullable = false, insertable = true, updatable = false)
    private Date created;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = LAST_MODIFIED_FIELD_NAME, nullable = false, insertable = true, updatable = true)
    private Date lastModified;
}
