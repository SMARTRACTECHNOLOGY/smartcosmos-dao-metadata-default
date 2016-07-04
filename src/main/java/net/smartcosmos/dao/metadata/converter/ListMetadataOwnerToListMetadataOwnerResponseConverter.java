package net.smartcosmos.dao.metadata.converter;

import java.util.List;
import java.util.stream.Collectors;

import net.smartcosmos.dao.metadata.domain.MetadataOwner;
import net.smartcosmos.dto.metadata.MetadataOwnerResponse;

public class ListMetadataOwnerToListMetadataOwnerResponseConverter extends ConversionServiceAwareConverter<List<MetadataOwner>,
    List<MetadataOwnerResponse>> {

    @Override
    public List<MetadataOwnerResponse> convert(List<MetadataOwner> metadataOwners) {

        return metadataOwners.stream()
            .map(o -> conversionService().convert(o, MetadataOwnerResponse.class))
            .collect(Collectors.toList());
    }
}
