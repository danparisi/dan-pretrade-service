package com.danservice.pretrade.adapter.outbound.client.validation;

import com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiCreateOrderDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ValidationOrderMapper {

    OrderValidationRequestDTO map(ApiCreateOrderDTO orderEntity);

}
