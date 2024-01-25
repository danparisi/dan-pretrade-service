package com.danservice.pretrade.api.v1;

import com.danservice.pretrade.api.v1.dto.CreateOrderDTO;
import com.danservice.pretrade.api.v1.dto.OrderDTO;
import com.danservice.pretrade.persistency.model.OrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderDTO map(OrderEntity orderEntity);

    OrderEntity map(CreateOrderDTO orderDTO);

    OrderEntity map(OrderDTO orderDTO);

}
