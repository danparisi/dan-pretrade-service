package com.danservice.pretrade.adapter;

import com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiCreateOrderDTO;
import com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiOrderDTO;
import com.danservice.pretrade.model.OrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApiOrderMapper {

    ApiOrderDTO map(OrderEntity orderEntity);

    OrderEntity map(ApiCreateOrderDTO orderDTO);

    OrderEntity map(ApiOrderDTO orderDTO);

}
