package net.smartcosmos.dao.metadata.converter;

import net.smartcosmos.dao.metadata.domain.MetadataOwner;
import net.smartcosmos.dto.metadata.MetadataOwnerResponse;
import net.smartcosmos.dto.metadata.Page;
import net.smartcosmos.dto.metadata.PageInformation;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpringDataPageMetadataOwnerToMetadataOwnerResponsePageConverter
    extends ConversionServiceAwareConverter<org.springframework.data.domain.PageImpl<MetadataOwner>, Page<MetadataOwnerResponse>> {

    @Inject
    private ConversionService conversionService;

    protected ConversionService conversionService() {
        return conversionService;
    }

    @Override
    public Page<MetadataOwnerResponse> convert(org.springframework.data.domain.PageImpl<MetadataOwner> page) {

        PageInformation pageInformation = PageInformation.builder()
            .number(page.getNumber() + 1)
            .totalElements(page.getTotalElements())
            .size(page.getNumberOfElements())
            .totalPages(page.getTotalPages())
            .build();

        List<MetadataOwnerResponse> data = page.getContent().stream()
            .map(entity -> conversionService.convert(entity, MetadataOwnerResponse.class))
            .collect(Collectors.toList());

        return Page.<MetadataOwnerResponse>builder()
            .data(data)
            .page(pageInformation)
            .build();
    }
}
