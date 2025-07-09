package com.delivery.reactive.delivery_service.service;

import com.delivery.reactive.delivery_service.dto.OrderRequestDTO;
import com.delivery.reactive.delivery_service.dto.OrderResponseDTO;
import com.delivery.reactive.delivery_service.model.Order;
import com.delivery.reactive.delivery_service.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final R2dbcEntityTemplate r2dbcEntityTemplate;
  private final WebClient customerClient;

  public Mono<OrderResponseDTO> createOrder(OrderRequestDTO dto) {
    return validateCustomer(dto.getCustomerId())
        .flatMap(
            valid -> {
              Order order =
                  Order.builder()
                      .id(UUID.randomUUID())
                      .customerId(dto.getCustomerId())
                      .dish(dto.getDish())
                      .status("PENDIENTE")
                      .createdAt(LocalDateTime.now())
                      .build();

              return r2dbcEntityTemplate
                  .insert(Order.class)
                  .using(order)
                  .doOnNext(o -> System.out.println("Pedido insertado: " + o))
                  .map(this::toResponse);
            });
  }

  private Mono<Boolean> validateCustomer(UUID customerId) {
    return customerClient
        .get()
        .uri("/customers/{id}", customerId)
        .exchangeToMono(
            response -> {
              if (response.statusCode().is2xxSuccessful()) {
                return Mono.just(true);
              } else if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                return Mono.error(
                    new IllegalArgumentException(
                        "El cliente con ID " + customerId + " no existe."));
              } else {
                return Mono.error(
                    new IllegalStateException(
                        "Error inesperado al validar cliente: " + response.statusCode()));
              }
            });
  }

  public Flux<OrderResponseDTO> listAllOrders() {
    return orderRepository.findAll().map(this::toResponse);
  }

  public Flux<OrderResponseDTO> getByStatus(String status) {
    return orderRepository.findByStatus(status).map(this::toResponse);
  }

  public Mono<OrderResponseDTO> updateStatus(UUID id, String status) {
    return orderRepository
        .findById(id)
        .flatMap(
            order -> {
              order.setStatus(status);
              return orderRepository.save(order);
            })
        .map(this::toResponse);
  }

  private OrderResponseDTO toResponse(Order order) {
    return OrderResponseDTO.builder()
        .id(order.getId())
        .customerId(order.getCustomerId())
        .dish(order.getDish())
        .status(order.getStatus())
        .createdAt(order.getCreatedAt())
        .build();
  }
}
