package com.delivery.reactive.delivery_service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.delivery.reactive.delivery_service.dto.OrderRequestDTO;
import com.delivery.reactive.delivery_service.dto.OrderResponseDTO;
import com.delivery.reactive.delivery_service.model.Order;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveInsertOperation;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OrderServiceTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private WebClient customerClient;

  @Mock private R2dbcEntityTemplate entityTemplate;

  @InjectMocks private OrderService orderService;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void createOrder_withValidCustomer_createsOrderSuccessfully() {
    UUID customerId = UUID.randomUUID();
    String dish = "Empanada de queso";
    OrderRequestDTO dto = new OrderRequestDTO(customerId, dish);

    Order createdOrder =
        Order.builder()
            .id(UUID.randomUUID())
            .customerId(customerId)
            .dish(dish)
            .status("PENDIENTE")
            .createdAt(LocalDateTime.now())
            .build();

    when(customerClient.get().uri(anyString(), eq(customerId)).exchangeToMono(any()))
        .thenReturn(Mono.just(true));

    ReactiveInsertOperation.ReactiveInsert<Order> insertMock =
        mock(ReactiveInsertOperation.ReactiveInsert.class);
    when(insertMock.using(any(Order.class))).thenReturn(Mono.just(createdOrder));
    when(entityTemplate.insert(Order.class)).thenReturn(insertMock);

    Mono<OrderResponseDTO> result = orderService.createOrder(dto);

    StepVerifier.create(result)
        .expectNextMatches(
            res ->
                res.getDish().equals(dish)
                    && res.getCustomerId().equals(customerId)
                    && res.getStatus().equals("PENDIENTE"))
        .verifyComplete(); // ← espera que todo se haya completado exitosamente
  }

  @Test
  void createOrder_withInvalidCustomer_shouldFail() {
    UUID customerId = UUID.randomUUID();
    String dish = "Empanada fantasma";
    OrderRequestDTO dto = new OrderRequestDTO(customerId, dish);

    // Resetear mocks si es necesario
    reset(customerClient);

    // Simular WebClient que devuelve error
    when(customerClient.get().uri(anyString(), eq(customerId)).exchangeToMono(any()))
        .thenReturn(
            Mono.error(
                new IllegalArgumentException("El cliente con ID " + customerId + " no existe.")));

    Mono<OrderResponseDTO> result = orderService.createOrder(dto);

    StepVerifier.create(result)
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().contains("no existe"))
        .verify();
  }
}
