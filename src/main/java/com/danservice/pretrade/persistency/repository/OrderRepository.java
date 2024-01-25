package com.danservice.pretrade.persistency.repository;

import com.danservice.pretrade.persistency.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

}
