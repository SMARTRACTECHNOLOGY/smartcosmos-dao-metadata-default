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
            .number(page.getNumber() + 1)
            .totalElements(page.getTotalElements())
            .size(page.getNumberOfElements())
            .totalPages(page.getTotalPages())
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }

}
