package com.danservice.pretrade.service;

import com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiCreateOrderDTO;
import com.danservice.pretrade.adapter.outbound.client.validation.ValidationClient;
import com.danservice.pretrade.adapter.outbound.client.validation.ValidationOrderMapper;
import com.danservice.pretrade.exception.OrderValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {
    private final ValidationClient validationClient;
    private final ValidationOrderMapper validationOrderMapper;

    public void validateForCreate(ApiCreateOrderDTO orderDTO) {
        var response = validationClient.validateOrder(validationOrderMapper.map(orderDTO));

        if (!response.isValid()) {
            throw new OrderValidationException(response.getErrors());
        }
    }
}
