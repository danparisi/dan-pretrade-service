package com.danservice.pretrade.service;

import com.danservice.pretrade.api.v1.OrderMapper;
import com.danservice.pretrade.api.v1.dto.CreateOrderDTO;
import com.danservice.pretrade.api.v1.dto.OrderDTO;
import com.danservice.pretrade.persistency.model.OrderEntity;
import com.danservice.pretrade.persistency.repository.OrderRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrdersService {
    @Value("${dan.topic.client-order}")
    private String clientOrdersTopic;
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final ValidationService validationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderDTO add(@NonNull @Valid CreateOrderDTO createOrderDTO) {
        validationService.validateForCreate(createOrderDTO);

        final OrderEntity storedOrderEntity = orderRepository.save(orderMapper.map(createOrderDTO));
        final OrderDTO storedOrderDTO = orderMapper.map(storedOrderEntity);

        kafkaTemplate.send(clientOrdersTopic, storedOrderDTO.getId().toString(), storedOrderDTO);

        return storedOrderDTO;
    }

    public Optional<OrderDTO> find(@NonNull UUID orderId) {
        return orderRepository
                .findById(orderId)
                .map(orderMapper::map);
    }

    public Collection<OrderDTO> findAll() {
        return orderRepository
                .findAll()
                .stream()
                .map(orderMapper::map)
                .collect(toSet());
    }

}
