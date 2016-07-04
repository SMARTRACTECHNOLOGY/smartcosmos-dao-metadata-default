package net.smartcosmos.dao.metadata.converter;

import java.util.HashSet;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwner;
import net.smartcosmos.dto.metadata.MetadataOwnerResponse;
import net.smartcosmos.dto.metadata.MetadataSingleResponse;
import net.smartcosmos.dto.metadata.PageInformation;

public abstract class AbstractPageConverter<S, T> implements GenericConverter {

    private Class<S> sourceClass;
    private Class<T> targetClass;

    protected AbstractPageConverter() {

//        Type typeA = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
//        Type typeB = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
//        this.sourceClass = (Class<S>) typeA;
//        this.targetClass = (Class<T>) typeB;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {

        Set<ConvertiblePair> convertiblePairs = new HashSet<>();
        convertiblePairs.add(new ConvertiblePair(MetadataEntity.class, MetadataSingleResponse.class));
        convertiblePairs.add(new ConvertiblePair(MetadataOwner.class, MetadataOwnerResponse.class));

        return convertiblePairs;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {

        System.out.println("Hello Converter!");

        ConvertiblePair test = new ConvertiblePair(sourceType.getType(), targetType.getType());

        if (getConvertibleTypes().contains(test)) {
            System.out.println("I can convert this.");
        }

            System.out.println("I can NOT convert this.");
//        if (sourceClass.equals(sourceType.getType())) {
//            return this.convert((S) source);
//        }

        return null;
    }

    protected PageInformation getPageInformation(S source) {

        if (source instanceof org.springframework.data.domain.Page) {
            org.springframework.data.domain.Page page = (org.springframework.data.domain.Page) source;

            return PageInformation.builder()
                .number(page.getNumber() + 1)
                .totalElements(page.getTotalElements())
                .size(page.getNumberOfElements())
                .totalPages(page.getTotalPages())
                .build();
        }

        return null;
    }

    public abstract T convert(S source);
}
