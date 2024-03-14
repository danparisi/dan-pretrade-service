package com.danservice.pretrade.adapter.outbound.client.validation;

import com.danservice.pretrade.domain.OrderType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

@Data
@Builder
@JsonInclude(NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderValidationRequestDTO {

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
