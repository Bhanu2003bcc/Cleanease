package com.mrer.cleanease.mapper;

import com.mrer.cleanease.dto.request.OrderItemRequest;
import com.mrer.cleanease.dto.response.OrderItemResponse;
import com.mrer.cleanease.entity.LaundryService;
import com.mrer.cleanease.entity.OrderItems;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class OrderItemsMapperTest {
    private OrderItemsMapper orderItemsMapper;

    @BeforeEach
    void setUp() {
        orderItemsMapper = new OrderItemsMapper();
    }

    @Test
    void toEntity_shouldMapFieldsCorrectly() {
        // Arrange
        OrderItemRequest request = new OrderItemRequest();
        request.setQuantity(3);

        LaundryService service = new LaundryService();
        service.setId(1L);
        service.setName("Dry Cleaning");

        // Act
        OrderItems result = orderItemsMapper.toEntity(request, service);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(3);
        assertThat(result.getService()).isEqualTo(service);
    }
    @Test
    void toEntity_shouldReturnNull_whenInputIsNull() {
        assertThat(orderItemsMapper.toEntity(null, null)).isNull();
        assertThat(orderItemsMapper.toEntity(new OrderItemRequest(), null)).isNull();
        assertThat(orderItemsMapper.toEntity(null, new LaundryService())).isNull();
    }

    @Test
    void toResponse_shouldMapFieldsCorrectly() {
        // Arrange
        LaundryService service = new LaundryService();
        service.setName("Wash & Fold");

        OrderItems item = new OrderItems();
        item.setId(10L);
        item.setQuantity(2);
        item.setService(service);

        // Act
        OrderItemResponse response = orderItemsMapper.toResponse(item);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getServiceName()).isEqualTo("Wash & Fold");
    }

    @Test
    void toResponse_shouldReturnNull_whenItemOrServiceIsNull() {
        assertThat(orderItemsMapper.toResponse(null)).isNull();

        OrderItems item = new OrderItems();
        item.setId(5L);
        item.setQuantity(2);
        item.setService(null);

        assertThat(orderItemsMapper.toResponse(item)).isNull(); // Prevents NPE
    }
}
