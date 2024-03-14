package com.danservice.pretrade.adapter.outbound.client.gfm;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient("dan-gfm-service")
public interface GfmClient {

    @GetMapping("orders/v1/{orderId}/status")
    GetOrderStatusResponseDTO getOrderStatus(@PathVariable UUID orderId);

}
