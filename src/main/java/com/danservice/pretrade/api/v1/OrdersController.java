package com.danservice.pretrade.api.v1;

import com.danservice.pretrade.api.v1.dto.BaseOrderResponseDTO;
import com.danservice.pretrade.api.v1.dto.CreateOrderDTO;
import com.danservice.pretrade.api.v1.dto.CreateOrderResponseDTO;
import com.danservice.pretrade.api.v1.dto.OrderDTO;
import com.danservice.pretrade.exception.OrderValidationException;
import com.danservice.pretrade.service.OrdersService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.danservice.pretrade.api.v1.OrdersController.BASE_ENDPOINT_ORDERS;
import static com.danservice.pretrade.api.v1.dto.ResultType.ERROR;
import static com.danservice.pretrade.api.v1.dto.ResultType.SUCCESS;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(BASE_ENDPOINT_ORDERS)
public class OrdersController {
    public static final String BASE_ENDPOINT_ORDERS = "/v1/orders";

    private final OrdersService ordersService;

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<BaseOrderResponseDTO> exceptionHandler(ValidationException exception) {
        return ResponseEntity
                .badRequest()
                .body(getExceptionBody(List.of(exception.getMessage())));
    }

    @ExceptionHandler(OrderValidationException.class)
    public ResponseEntity<BaseOrderResponseDTO> exceptionHandler(OrderValidationException exception) {
        return ResponseEntity
                .badRequest()
                .body(getExceptionBody(exception.getErrors()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> get(@NotNull @PathVariable UUID orderId) {
        log.info("Returning order [{}]", orderId);

        return ordersService
                .find(orderId)
                .map(ResponseEntity::ok)
                .orElse(noContent().build());
    }

    @GetMapping
    public ResponseEntity<Collection<OrderDTO>> getAll() {
        log.info("Returning all orders");

        return ok(ordersService.findAll());
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponseDTO> add(@RequestBody @Valid CreateOrderDTO orderDTO) {
        log.info("Adding order [{}]", orderDTO);
        OrderDTO newOrder = ordersService.add(orderDTO);

        CreateOrderResponseDTO response = CreateOrderResponseDTO.builder()
                .order(newOrder)
                .result(SUCCESS).build();

        return status(CREATED).body(response);
    }

    private static BaseOrderResponseDTO getExceptionBody(List<String> exception) {
        return BaseOrderResponseDTO.builder()
                .result(ERROR)
                .errors(exception).build();
    }
}
