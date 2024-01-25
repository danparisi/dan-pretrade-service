package com.danservice.pretrade.service;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderValidationException extends RuntimeException {

    List<String> errors;

}
