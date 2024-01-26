package com.danservice.pretrade.client.validation;

import com.danservice.pretrade.api.v1.dto.CreateOrderDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ValidationOrderMapper {

    OrderValidationRequestDTO map(CreateOrderDTO orderEntity);

}
