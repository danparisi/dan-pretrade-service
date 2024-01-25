package com.danservice.pretrade.api.v1.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@RequiredArgsConstructor
@SuperBuilder(toBuilder = true)
public class BaseOrderResponseDTO {

    @NotNull
    private ResultType result;

    private List<String> errors;

}
