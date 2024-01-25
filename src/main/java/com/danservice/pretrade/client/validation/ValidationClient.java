package com.danservice.pretrade.client.validation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("dan-validation-service")
public interface ValidationClient {

    @PostMapping("v1/orders/validate")
    ValidationOrderResponseDTO validateOrder(@RequestBody ValidationOrderRequestDTO validationOrderRequestDTO);

}
