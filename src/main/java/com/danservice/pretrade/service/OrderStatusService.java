package com.danservice.pretrade.service;

import com.danservice.pretrade.adapter.outbound.client.gfm.GfmClient;
import com.danservice.pretrade.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusService {
    private final GfmClient gfmClient;

    public OrderStatus getOrderStatus(UUID orderId) {
        return gfmClient
                .getOrderStatus(orderId)
                .getCurrentStatus();
    }
}
