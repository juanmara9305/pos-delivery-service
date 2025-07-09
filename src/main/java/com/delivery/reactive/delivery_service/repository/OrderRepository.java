package com.delivery.reactive.delivery_service.repository;

import com.delivery.reactive.delivery_service.model.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import java.util.UUID;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, UUID> {
    Flux<Order> findByStatus(String status);
}
