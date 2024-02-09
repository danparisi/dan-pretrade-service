package com.danservice.pretrade.adapter.inbound.api.order.v1.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@RequiredArgsConstructor
@SuperBuilder(toBuilder = true)
public class ApiBaseOrderResponseDTO {

    @NotNull
    private ApiResultType result;

    private List<String> errors;

}
