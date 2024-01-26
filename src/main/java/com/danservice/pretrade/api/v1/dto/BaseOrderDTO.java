package com.danservice.pretrade.api.v1.dto;

import com.danservice.pretrade.domain.OrderType;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
@SuperBuilder(toBuilder = true)
public class BaseOrderDTO {

    @NotNull
    private OrderType type;

    @Min(0)
    @NotNull
    private int quantity;
    @NotNull
    @DecimalMin("0.0001")
    private BigDecimal price;
    @NotEmpty
    @Size(min = 1, max = 20)
    private String instrument;

}
