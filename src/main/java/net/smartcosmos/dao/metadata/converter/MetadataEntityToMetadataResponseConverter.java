package net.smartcosmos.dao.metadata.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.util.UuidUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MetadataEntityToMetadataResponseConverter
    implements Converter<MetadataEntity, MetadataResponse>, FormatterRegistrar {

    @Override
    public MetadataResponse convert(MetadataEntity entity) {

        Map<String, Object> metadata = new HashMap<>();
        Object o = parseValue(entity);
        metadata.put(entity.getKeyName(), o);

        return MetadataResponse.builder()
            .urn(UuidUtil.getUrnFromUuid(entity.getId()))
            .ownerType(entity.getOwnerType())
            .ownerUrn(UuidUtil.getUrnFromUuid(entity.getOwnerId()))
            .metadata(metadata)
            .tenantUrn(UuidUtil.getAccountUrnFromUuid(entity.getTenantId()))
            .build();
    }

    // TODO: DUPLICATE - refactor this into a helper class when everything works
    private Object parseValue(MetadataEntity entity) {
        Object o = null;

        // Boolean
        if (Boolean.class.getSimpleName().equals(entity.getDataType())) {
            o = Boolean.parseBoolean(entity.getValue());
        }
        // Integer
        if (Integer.class.getSimpleName().equals(entity.getDataType())) {
            o = Integer.parseInt(entity.getValue());
        }
        // Long
        if (Long.class.getSimpleName().equals(entity.getDataType())) {
            o = Long.parseLong(entity.getValue());
        }
        // String
        if (String.class.getSimpleName().equals(entity.getDataType())) {
            o = entity.getValue();
        }
        // JSONObject
        if (JsonNode.class.getSimpleName().equals(entity.getDataType())) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
                o = mapper.readTree(entity.getValue());
            } catch (IOException ex) {
                log.debug(ex.getMessage());
                log.warn("Unparseable JSON object in metadata value, returning string instead!");
                o = entity.getValue();
            }
        }
        // Catch-all: everything else will be returned as String
        if (o == null) {
            o = entity.getValue();
        }
        return o;
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
