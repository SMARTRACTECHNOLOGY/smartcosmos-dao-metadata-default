package net.smartcosmos.dao.metadata.converter;

import java.util.ArrayList;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.runners.*;
import org.springframework.data.domain.PageImpl;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.util.MetadataPersistenceUtil;
import net.smartcosmos.dto.metadata.MetadataResponse;
import net.smartcosmos.dto.metadata.Page;
import net.smartcosmos.dto.metadata.PageInformation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SpringPageToPageInformationConverterTest {

    @InjectMocks
    SpringPageToPageInformationConverter converter;

    @Test
    public void testConversionService() throws Exception {

        List<MetadataEntity> content = new ArrayList<>();
        content.add(mock(MetadataEntity.class));
        content.add(mock(MetadataEntity.class));

        org.springframework.data.domain.PageImpl<MetadataEntity> entityPage = new PageImpl<>(content);
        PageInformation page = converter.convert(entityPage);

        assertNotNull(page);
        assertEquals(1, page.getNumber());
        assertEquals(2, page.getSize());
        assertEquals(1, page.getTotalPages());
        assertEquals(2, page.getTotalElements());
    }

    @Test
    public void thatEmptyPageConversionSucceeds() {

        List<MetadataEntity> content = new ArrayList<>();

        org.springframework.data.domain.PageImpl<MetadataEntity> entityPage = new PageImpl<>(content);
        PageInformation page = converter.convert(entityPage);

        assertNotNull(page);
        assertEquals(0, page.getSize());
        assertEquals(0, page.getNumber());
        assertEquals(0, page.getTotalPages());
        assertEquals(0, page.getTotalElements());

        Page<MetadataResponse> emptyPage = MetadataPersistenceUtil.emptyPage();
        assertEquals(emptyPage.getPage(), page);
    }
}
