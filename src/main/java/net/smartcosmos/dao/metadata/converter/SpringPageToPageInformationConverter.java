package net.smartcosmos.dao.metadata.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import net.smartcosmos.dto.metadata.PageInformation;

@Component
public class SpringPageToPageInformationConverter implements Converter<PageImpl<?>, PageInformation>, FormatterRegistrar {

    @Override
    public PageInformation convert(PageImpl<?> page) {

        return PageInformation.builder()
            .number((page.getTotalElements() > 0 ? page.getNumber() + 1 : 0))
            .totalElements(page.getTotalElements())
            .size(page.getNumberOfElements())
            .totalPages((page.getNumberOfElements() > 0 ? page.getTotalPages() : 0))
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {

        registry.addConverter(this);
    }

}
