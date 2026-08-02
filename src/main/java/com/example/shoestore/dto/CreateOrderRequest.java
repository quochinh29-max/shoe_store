package com.example.shoestore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;

    private String note;

    /**
     * Mã voucher (không bắt buộc)
     */
    private String voucherCode;

    @NotEmpty(message = "Giỏ hàng không được để trống")
    @Valid
    private List<OrderItemRequest> items;
}
