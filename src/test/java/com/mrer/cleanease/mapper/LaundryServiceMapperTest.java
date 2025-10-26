package com.mrer.cleanease.mapper;

import com.mrer.cleanease.dto.request.LaundryServiceRequest;
import com.mrer.cleanease.dto.response.LaundryServiceResponse;
import com.mrer.cleanease.entity.Enums;
import com.mrer.cleanease.entity.LaundryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class LaundryServiceMapperTest {

    private LaundryServiceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new LaundryServiceMapper();
    }

    @Test
    void testToEntity_NullRequest_ReturnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void testToEntity_ValidRequest_ReturnsMappedEntity() {
        LaundryServiceRequest dto = new LaundryServiceRequest();
        dto.setName("Wash & Fold");
        dto.setDescription("Standard laundry service");
        dto.setPrice(new BigDecimal(150.0));
        dto.setCategory(Enums.ServiceCategory.LAUNDRY);

        LaundryService entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("Wash & Fold", entity.getName());
        assertEquals("Standard laundry service", entity.getDescription());
        assertEquals(new BigDecimal(150.0), entity.getPrice());
        assertEquals(Enums.ServiceCategory.LAUNDRY, entity.getCategory());
    }
    @Test
    void testToResponse_NullEntity_ReturnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void testToResponse_ValidEntity_ReturnsMappedResponse() {
        LaundryService entity = new LaundryService();
        entity.setId(10L);
        entity.setName("Dry Clean");
        entity.setDescription("Premium dry cleaning");
        entity.setPrice(new BigDecimal(250.0));

        LaundryServiceResponse response = mapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Dry Clean", response.getName());
        assertEquals("Premium dry cleaning", response.getDescription());
        assertEquals(new BigDecimal(250.0), response.getPrice());
    }
}
