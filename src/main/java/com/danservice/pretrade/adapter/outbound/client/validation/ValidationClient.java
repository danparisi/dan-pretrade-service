package com.danservice.pretrade.adapter.outbound.client.validation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("dan-validation-service")
public interface ValidationClient {

    @PostMapping("orders/v1/validate")
    OrderValidationResponseDTO validateOrder(@RequestBody OrderValidationRequestDTO orderValidationRequestDTO);

}
