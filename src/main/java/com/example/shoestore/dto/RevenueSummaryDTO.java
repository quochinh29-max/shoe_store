package com.example.shoestore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueSummaryDTO {

    private BigDecimal totalRevenue;
    private long totalOrders;
    private long completedOrders;
    private long pendingOrders;
    private long shippingOrders;
    private long cancelledOrders;
    private BigDecimal averageOrderValue;
}
