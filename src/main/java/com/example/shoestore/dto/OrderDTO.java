package com.example.shoestore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private Integer id;
    private Integer customerId;
    private String customerName;
    private String customerEmail;

    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    private String orderStatus;
    private String paymentMethod;
    private String paymentStatus;

    private String shippingAddress;
    private String note;
    private String voucherCode;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Chỉ có giá trị khi lấy chi tiết 1 đơn hàng
    private List<OrderItemDTO> items;
}
