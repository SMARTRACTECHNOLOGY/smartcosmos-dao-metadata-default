package net.smartcosmos.dao.metadata.converter;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

abstract class ConversionServiceAwareConverter<S, T> implements Converter<S, T> {

    @Inject
    private ConversionService conversionService;

    protected ConversionService conversionService() {
        return conversionService;
    }

    @PostConstruct
    private void register() {
        if (conversionService instanceof ConverterRegistry) {
            ((ConverterRegistry) conversionService).addConverter(this);
        } else {
            throw new IllegalStateException("Can't register Converter to ConverterRegistry");
        }
    }
}
