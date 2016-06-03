package net.smartcosmos.dao.metadata.converter;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dto.metadata.MetadataQueryMatchResponse;
import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.util.UuidUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MetadataEntityToMetadataQueryMatchResponseConverter
    implements Converter<MetadataEntity, MetadataQueryMatchResponse>, FormatterRegistrar {

    @Override
    public MetadataQueryMatchResponse convert(MetadataEntity entity) {

        return MetadataQueryMatchResponse.builder()
            .urn(UuidUtil.getUrnFromUuid(entity.getReferenceId()))
            .build();
    }

    public List convertAll(Iterable<MetadataEntity> entities) {

        List<MetadataQueryMatchResponse> convertedList = new ArrayList<>();
        for (MetadataEntity entity : entities) {
            convertedList.add(convert(entity));
        }

        return convertedList;
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
