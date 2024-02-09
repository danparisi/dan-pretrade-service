package com.danservice.pretrade.adapter.outbound.kafka.clientorder.v1;

import com.danservice.pretrade.adapter.outbound.kafka.clientorder.v1.dto.KafkaClientOrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    @Value("${dan.topic.client-order}")
    private String clientOrdersTopic;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendClientOrder(KafkaClientOrderDTO orderDTO) {
        String key = orderDTO.getId().toString();
        kafkaTemplate.send(clientOrdersTopic, key, orderDTO);

    }
}
