package com.danservice.pretrade.client.validation;

import com.danservice.pretrade.api.v1.dto.OrderType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ValidationOrderRequestDTO {

    @NotEmpty
    private OrderType type;

    @NotNull
    private int quantity;
    @NotNull
    private BigDecimal price;
    @NotEmpty
    private String instrument;

}
