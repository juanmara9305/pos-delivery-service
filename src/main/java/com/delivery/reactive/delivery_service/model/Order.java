package com.delivery.reactive.delivery_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("orders")
public class Order {
    @Id
    private UUID id;
    private UUID customerId; // Referencia al cliente
    private String dish;
    private String status;
    private LocalDateTime createdAt;
}