package net.smartcosmos.dao.metadata.converter;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

import org.springframework.core.convert.ConversionService;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dto.metadata.MetadataSingleResponse;
import net.smartcosmos.dto.metadata.Page;
import net.smartcosmos.dto.metadata.PageInformation;

@Component
public class SpringDataPageToMetadataSingleResponsePageConverter
    extends AbstractPageConverter<org.springframework.data.domain.Page<MetadataEntity>, Page<MetadataSingleResponse>> implements FormatterRegistrar {

    @Inject
    private ConversionService conversionService;

    protected ConversionService conversionService() {
        return conversionService;
    }

    @Override
    public Page<MetadataSingleResponse> convert(org.springframework.data.domain.Page<MetadataEntity> page) {

        PageInformation pageInformation = super.getPageInformation(page);

        List<MetadataSingleResponse> data = page.getContent().stream()
            .map(entity -> conversionService.convert(entity, MetadataSingleResponse.class))
            .collect(Collectors.toList());

        return Page.<MetadataSingleResponse>builder()
            .data(data)
            .page(pageInformation)
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
