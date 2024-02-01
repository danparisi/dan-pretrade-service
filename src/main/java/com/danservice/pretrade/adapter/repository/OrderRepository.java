package com.danservice.pretrade.adapter.repository;

import com.danservice.pretrade.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

}
