package com.danservice.pretrade.adapter;

import com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiOrderDTO;
import com.danservice.pretrade.adapter.outbound.kafka.clientorder.v1.dto.KafkaClientOrderDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface KafkaClientOrderMapper {

    KafkaClientOrderDTO map(ApiOrderDTO apiOrderDTO);

}
