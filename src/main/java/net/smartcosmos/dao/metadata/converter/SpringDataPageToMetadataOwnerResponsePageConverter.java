package net.smartcosmos.dao.metadata.converter;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

import org.springframework.core.convert.ConversionService;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.metadata.domain.MetadataOwner;
import net.smartcosmos.dto.metadata.MetadataOwnerResponse;
import net.smartcosmos.dto.metadata.Page;
import net.smartcosmos.dto.metadata.PageInformation;

@Component
public class SpringDataPageToMetadataOwnerResponsePageConverter
    extends AbstractPageConverter<org.springframework.data.domain.Page<MetadataOwner>, Page<MetadataOwnerResponse>> implements FormatterRegistrar {

    @Inject
    private ConversionService conversionService;

    protected ConversionService conversionService() {
        return conversionService;
    }

    @Override
    public Page<MetadataOwnerResponse> convert(org.springframework.data.domain.Page<MetadataOwner> page) {

        System.out.println("HELLO CONVERTER");

        PageInformation pageInformation = super.getPageInformation(page);

        List<MetadataOwnerResponse> data = page.getContent().stream()
            .map(entity -> conversionService.convert(entity, MetadataOwnerResponse.class))
            .collect(Collectors.toList());

        return Page.<MetadataOwnerResponse>builder()
            .data(data)
            .page(pageInformation)
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
