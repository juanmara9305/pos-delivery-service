package com.delivery.reactive.delivery_service.controller;

import com.delivery.reactive.delivery_service.dto.OrderRequestDTO;
import com.delivery.reactive.delivery_service.dto.OrderResponseDTO;
import com.delivery.reactive.delivery_service.service.OrderService;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public Mono<OrderResponseDTO> create(@RequestBody OrderRequestDTO dto) {
    return orderService.createOrder(dto);
  }

  @GetMapping
  public Flux<OrderResponseDTO> listAll() {
    return orderService.listAllOrders();
  }

  @GetMapping("/status/{status}")
  public Flux<OrderResponseDTO> getByStatus(@PathVariable String status) {
    return orderService.getByStatus(status);
  }

  @PutMapping("/{id}/status")
  public Mono<OrderResponseDTO> updateStatus(@PathVariable UUID id, @RequestParam String status) {
    return orderService.updateStatus(id, status);
  }
}
