package com.danservice.pretrade.service;

import com.danservice.pretrade.api.v1.dto.CreateOrderDTO;
import com.danservice.pretrade.client.validation.ValidationClient;
import com.danservice.pretrade.client.validation.ValidationOrderMapper;
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

    public void validateForCreate(CreateOrderDTO orderDTO) {
        var response = validationClient.validateOrder(validationOrderMapper.map(orderDTO));

        if (!response.isValid()) {
            throw new OrderValidationException(response.getErrors());
        }
    }
}
