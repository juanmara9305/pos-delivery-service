package com.delivery.reactive.delivery_service.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class OrderResponseDTO {
    private UUID id;
    private UUID customerId;
    private String dish;
    private String status;
    private LocalDateTime createdAt;
}
