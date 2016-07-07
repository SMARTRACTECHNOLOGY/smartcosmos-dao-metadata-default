package net.smartcosmos.dao.metadata.util;

import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;

import net.smartcosmos.dao.metadata.SortOrder;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dto.metadata.Page;
import net.smartcosmos.dto.metadata.PageInformation;

import static net.smartcosmos.dao.metadata.domain.MetadataEntity.*;

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

        if (StringUtils.equalsIgnoreCase(KEY_NAME_FIELD_NAME, fieldName)) {
            return KEY_NAME_FIELD_NAME;
        }

        if (StringUtils.equalsIgnoreCase(VALUE_FIELD_NAME, fieldName)) {
            return VALUE_FIELD_NAME;
        }

        if (StringUtils.equalsIgnoreCase(DATA_TYPE_FIELD_NAME, fieldName)) {
            return DATA_TYPE_FIELD_NAME;
        }

        if (StringUtils.equalsIgnoreCase("tenantUrn", fieldName) || StringUtils.equalsIgnoreCase(TENANT_ID_FIELD_NAME, fieldName)) {
            return MetadataOwnerEntity.TENANT_ID_FIELD_NAME;
        }

        if (StringUtils.equalsIgnoreCase(CREATED_FIELD_NAME, fieldName)) {
            return CREATED_FIELD_NAME;
        }

        if (StringUtils.equalsIgnoreCase(LAST_MODIFIED_FIELD_NAME, fieldName)) {
            return LAST_MODIFIED_FIELD_NAME;
        }

        if (StringUtils.equalsIgnoreCase(OWNER_TYPE_FIELD_NAME, fieldName)) {
            return MetadataOwnerEntity.OWNER_TYPE_FIELD_NAME;
        }

        if (StringUtils.equalsIgnoreCase("ownerUrn", fieldName) || StringUtils.equalsIgnoreCase(OWNER_ID_FIELD_NAME, fieldName)) {
            return MetadataOwnerEntity.OWNER_ID_FIELD_NAME;
        }

        return fieldName;
    }

    /**
     * Checks if a given field name exists in a given class.
     *
     * @param fieldName the field name
     * @param clazz the class
     * @return {@code true} if the field exists
     */
    public static boolean isFieldInClass(String fieldName, Class clazz) {

        try {
            clazz.getDeclaredField(fieldName);
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
     * @return the case-corrected field name if it exists, {@code created} otherwise
     */
    public static String getSortByFieldName(String sortBy) {

        return getSortByFieldName(sortBy, CREATED_FIELD_NAME);
    }

    /**
     * Gets a valid field name for a {@code sortBy} query in the {@link MetadataEntity} data base.
     * The input field name is case-corrected and replaced by {@code id} if it does not exist in the entity class.
     *
     * @param sortBy the input field name
     * @param defaultFieldName the input field name to use if {@code sortBy} is blank
     * @return the case-corrected field name if it exists, {@code id} otherwise
     */
    public static String getSortByFieldName(String sortBy, String defaultFieldName) {
        sortBy = normalizeFieldName(sortBy);

        if (StringUtils.isBlank(sortBy) || (!isFieldInClass(sortBy, MetadataEntity.class) && !isFieldInClass(sortBy, MetadataOwnerEntity.class))) {
            sortBy = defaultFieldName;
        }

        if (isFieldInClass(sortBy, MetadataOwnerEntity.class)) {
            sortBy = "owner." + sortBy;
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
