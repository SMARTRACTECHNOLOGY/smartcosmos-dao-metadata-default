package net.smartcosmos.dao.metadata.util;

import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.dto.metadata.MetadataSingleResponse;
import net.smartcosmos.dto.metadata.Page;

public class MetadataPersistenceUtil {

    /**
     * Creates an empty {@link Page<MetadataResponse>} instance.
     *
     * @return the empty page
     */
    public static Page<MetadataSingleResponse> emptyPage() {

        return Page.<MetadataSingleResponse>builder().build();
    }
}
