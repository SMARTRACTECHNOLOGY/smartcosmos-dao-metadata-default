package net.smartcosmos.dao.metadata.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Entity(name = "metadata")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Data
@EntityListeners({ AuditingEntityListener.class })
@Table(name = "metadata", uniqueConstraints = @UniqueConstraint(columnNames = { "systemUuid", "accountUuid" }) )
public class MetadataEntity {

    private static final int UUID_LENGTH = 16;
    private static final int KEY_LENGTH = 255;
    private static final int DATA_TYPE_LENGTH = 255;
    private static final int ENTITY_REFERENCE_TYPE_LENGTH = 255;
    private static final int RAW_VALUE_LENGTH = 767;
    private static final int MONIKER_LENGTH = 2048;

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-binary")
    @Column(name = "systemUuid", length = UUID_LENGTH)
    private UUID id;

    @NotNull
    @Type(type = "uuid-binary")
    @Column(name = "accountUuid", length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID accountId;

    @Size(max = DATA_TYPE_LENGTH)
    @Column(name = "dataType", length = DATA_TYPE_LENGTH, nullable = true, updatable = true)
    private String dataType;

    @NotEmpty
    @Size(max = ENTITY_REFERENCE_TYPE_LENGTH)
    @Column(name = "entityReferenceType", length = DATA_TYPE_LENGTH, nullable = false, updatable = false)
    private String entityReferenceType;

    @NotNull
    @Type(type = "uuid-binary")
    @Column(name = "referenceUuid", length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID referenceId;

    @NotEmpty
    @Size(max = KEY_LENGTH)
    @Column(name = "metadata_key", length = KEY_LENGTH, nullable = false, updatable = false)
    private String key;

    @NotEmpty
    @Size(max = RAW_VALUE_LENGTH)
    @Column(name = "rawValue", length = RAW_VALUE_LENGTH, nullable = false, updatable = true)
    protected String rawValue;

    @CreatedDate
    @Column(name = "createdTimestamp", insertable = true, updatable = false)
    private Long created;

    @LastModifiedDate
//    @Column(name = "lastModifiedTimestamp", insertable = false, updatable = true) // lastModified only set on update, might be used later
    // lastModified already set on create (v2 compatibility)
    @Column(name = "lastModifiedTimestamp", nullable = false, insertable = true, updatable = true)
    private Long lastModified;

    @Size(max = MONIKER_LENGTH)
    @Column(name = "moniker", length = MONIKER_LENGTH, nullable = true, updatable = true)
    private String moniker;
}
