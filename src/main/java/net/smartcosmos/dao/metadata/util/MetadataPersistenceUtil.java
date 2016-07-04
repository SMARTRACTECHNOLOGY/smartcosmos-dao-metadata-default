package net.smartcosmos.dao.metadata.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;

import net.smartcosmos.dao.metadata.SortOrder;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dto.metadata.Page;
import net.smartcosmos.dto.metadata.PageInformation;

public class MetadataPersistenceUtil {

    /**
     * Creates an empty {@link Page<T>} instance.
     *
     * @return the empty page
     */
    public static <T> Page<T> emptyPage() {

        PageInformation pageInformation = PageInformation.builder().build();

        return Page.<T>builder()
            .page(pageInformation)
            .build();
    }

    /**
     * Transforms a field name for a sorted query to a valid case-sensitive field name that exists in the entity class.
     * Returns the input field name, if it does not exist in the entity class.
     *
     * @param fieldName the input field name
     * @return the case-corrected field name
     */
    public static String normalizeFieldName(String fieldName) {

        if (StringUtils.equalsIgnoreCase("keyName", fieldName)) {
            return "keyName";
        }

        if (StringUtils.equalsIgnoreCase("value", fieldName)) {
            return "value";
        }

        if (StringUtils.equalsIgnoreCase("dataType", fieldName)) {
            return "dataType";
        }

        if (StringUtils.equalsIgnoreCase("tenantUrn", fieldName) || StringUtils.equalsIgnoreCase("tenantId", fieldName)) {
            return "tenantId";
        }

        if (StringUtils.equalsIgnoreCase("created", fieldName)) {
            return "created";
        }

        if (StringUtils.equalsIgnoreCase("lastModified", fieldName)) {
            return "lastModified";
        }

        if (StringUtils.equalsIgnoreCase("ownerType", fieldName)) {
            return "ownerType";
        }

        if (StringUtils.equalsIgnoreCase("ownerUrn", fieldName) || StringUtils.equalsIgnoreCase("ownerId", fieldName)) {
            return "ownerId";
        }

        return fieldName;
    }

    /**
     * Checks if a given field name exists in {@link MetadataEntity}.
     *
     * @param fieldName the field name
     * @return {@code true} if the field exists
     */
    public static boolean isThingEntityField(String fieldName) {

        try {
            MetadataEntity.class.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return false;
        }

        return true;
    }

    /**
     * Gets a valid field name for a {@code sortBy} query in the {@link MetadataEntity} data base.
     * The input field name is case-corrected and replaced by {@code id} if it does not exist in the entity class.
     *
     * @param sortBy the input field name
     * @return the case-corrected field name if it exists, {@code id} otherwise
     */
    public static String getSortByFieldName(String sortBy) {
        sortBy = normalizeFieldName(sortBy);
        if (StringUtils.isBlank(sortBy) || !isThingEntityField(sortBy)) {
            sortBy = "created";
        }
        return sortBy;
    }

    /**
     * Converts the {@link SortOrder} value to a Spring-compatible {@link org.springframework.data.domain.Sort.Direction} sort direction.
     *
     * @param sortOrder the sort order
     * @return the Spring sort direction
     */
    public static Sort.Direction getSortDirection(SortOrder sortOrder) {
        Sort.Direction direction = Sort.DEFAULT_DIRECTION;
        if (sortOrder != null) {
            switch (sortOrder) {
                case ASC:
                    direction = Sort.Direction.ASC;
                    break;
                case DESC:
                    direction = Sort.Direction.DESC;
                    break;
            }
        }

        return direction;
    }
}
