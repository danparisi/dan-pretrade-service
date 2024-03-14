package com.danservice.pretrade.service;

import com.danservice.pretrade.adapter.ApiOrderMapper;
import com.danservice.pretrade.adapter.KafkaClientOrderMapper;
import com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiCreateOrderDTO;
import com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiOrderDTO;
import com.danservice.pretrade.adapter.outbound.kafka.clientorder.v1.KafkaProducer;
import com.danservice.pretrade.adapter.repository.OrderRepository;
import com.danservice.pretrade.model.OrderEntity;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final KafkaProducer kafkaProducer;
    private final ApiOrderMapper apiOrderMapper;
    private final OrderRepository orderRepository;
    private final ValidationService validationService;
    private final KafkaClientOrderMapper kafkaClientOrderMapper;

    public ApiOrderDTO add(@NonNull @Valid ApiCreateOrderDTO createOrderDTO) {
        validationService.validateForCreate(createOrderDTO);

        final OrderEntity storedOrderEntity = orderRepository.save(apiOrderMapper.map(createOrderDTO));
        final ApiOrderDTO storedOrderDTO = apiOrderMapper.map(storedOrderEntity);

        kafkaProducer.sendClientOrder(
                kafkaClientOrderMapper.map(storedOrderDTO));

        log.info("Order [{}] created", storedOrderDTO.getId());

        return storedOrderDTO;
    }

    public Optional<ApiOrderDTO> find(@NonNull UUID orderId) {
        return orderRepository
                .findById(orderId)
                .map(apiOrderMapper::map);
    }

    public Collection<ApiOrderDTO> findAll() {
        return orderRepository
                .findAll()
                .stream()
                .map(apiOrderMapper::map)
                .collect(toSet());
    }

}
