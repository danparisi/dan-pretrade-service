package com.danservice.pretrade.adapter.inbound.api.order.v1;

import com.danservice.pretrade.adapter.inbound.api.order.v1.dto.*;
import com.danservice.pretrade.exception.OrderValidationException;
import com.danservice.pretrade.service.OrderStatusService;
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

import static com.danservice.pretrade.adapter.inbound.api.order.v1.OrdersController.BASE_ENDPOINT_ORDERS;
import static com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiResultType.ERROR;
import static com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiResultType.SUCCESS;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(BASE_ENDPOINT_ORDERS)
public class OrdersController {
    public static final String BASE_ENDPOINT_ORDERS = "/orders/v1";

    private final OrdersService ordersService;
    private final OrderStatusService orderStatusService;

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiBaseOrderResponseDTO> exceptionHandler(ValidationException exception) {
        return ResponseEntity
                .badRequest()
                .body(getExceptionBody(List.of(exception.getMessage())));
    }

    @ExceptionHandler(OrderValidationException.class)
    public ResponseEntity<ApiBaseOrderResponseDTO> exceptionHandler(OrderValidationException exception) {
        return ResponseEntity
                .badRequest()
                .body(getExceptionBody(exception.getErrors()));
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<ApiOrderStatusResponseDTO> getStatus(@NotNull @PathVariable UUID orderId) {
        var orderStatus = orderStatusService.getOrderStatus(orderId);

        log.info("Returning status [{}] for order order [{}]", orderStatus, orderId);

        return ok(ApiOrderStatusResponseDTO.builder()
                .status(orderStatus).build());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiOrderDTO> get(@NotNull @PathVariable UUID orderId) {
        log.info("Returning order [{}]", orderId);

        return ordersService
                .find(orderId)
                .map(ResponseEntity::ok)
                .orElse(noContent().build());
    }

    @GetMapping
    public ResponseEntity<Collection<ApiOrderDTO>> getAll() {
        log.info("Returning all orders");

        return ok(ordersService.findAll());
    }

    @PostMapping
    public ResponseEntity<ApiCreateOrderResponseDTO> add(@RequestBody @Valid ApiCreateOrderDTO orderDTO) {
        log.info("Adding order [{}]", orderDTO);
        ApiOrderDTO newOrder = ordersService.add(orderDTO);

        ApiCreateOrderResponseDTO response = ApiCreateOrderResponseDTO.builder()
                .order(newOrder)
                .result(SUCCESS).build();

        return status(CREATED).body(response);
    }

    private static ApiBaseOrderResponseDTO getExceptionBody(List<String> exception) {
        return ApiBaseOrderResponseDTO.builder()
                .result(ERROR)
                .errors(exception).build();
    }
}
