package com.danservice.pretrade;

import com.danservice.pretrade.adapter.outbound.kafka.clientorder.v1.dto.KafkaClientOrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

@Slf4j
@EnableKafka
@EnableFeignClients
@SpringBootApplication
@RegisterReflectionForBinding(KafkaClientOrderDTO.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication
                .run(Application.class, args);
    }

}
