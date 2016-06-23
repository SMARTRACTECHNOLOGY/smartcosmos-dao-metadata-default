package net.smartcosmos.dao.metadata.converter;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dto.metadata.MetadataSingleResponse;
import net.smartcosmos.dto.metadata.Page;
import net.smartcosmos.dto.metadata.PageInformation;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

@Component
public class SpringDataPageToMetadataSingleResponsePageConverter
    extends ConversionServiceAwareConverter<org.springframework.data.domain.Page<MetadataEntity>, Page<MetadataSingleResponse>> {

    @Inject
    private ConversionService conversionService;

    protected ConversionService conversionService() {
        return conversionService;
    }

    @Override
    public Page<MetadataSingleResponse> convert(org.springframework.data.domain.Page<MetadataEntity> page) {

        PageInformation pageInformation = PageInformation.builder()
            .number(page.getNumber() + 1)
            .totalElements(page.getTotalElements())
            .size(page.getNumberOfElements())
            .totalPages(page.getTotalPages())
            .build();

        List<MetadataSingleResponse> data = page.getContent().stream()
            .map(entity -> conversionService.convert(entity, MetadataSingleResponse.class))
            .collect(Collectors.toList());

        return Page.<MetadataSingleResponse>builder()
            .data(data)
            .page(pageInformation)
            .build();
    }
}
