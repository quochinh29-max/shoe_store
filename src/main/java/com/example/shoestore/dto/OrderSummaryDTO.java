package com.example.shoestore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryDTO {

    private Integer id;
    private BigDecimal finalAmount;
    private String orderStatus;
    private String paymentStatus;
    private LocalDateTime createdAt;
}
