package com.danservice.pretrade.adapter.outbound.kafka.clientorder.v1;

import com.danservice.pretrade.adapter.outbound.kafka.clientorder.v1.dto.KafkaClientOrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {
    @Value("${dan.topic.client-order}")
    private String clientOrdersTopic;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendClientOrder(KafkaClientOrderDTO kafkaClientOrderDTO) {
        String key = kafkaClientOrderDTO.getId().toString();
        log.info("Sending client order [{}] against [{}] topic", kafkaClientOrderDTO, clientOrdersTopic);

        kafkaTemplate.send(clientOrdersTopic, key, kafkaClientOrderDTO);
    }
}
