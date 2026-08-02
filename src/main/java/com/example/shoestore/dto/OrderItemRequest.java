package com.example.shoestore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {

    @NotNull(message = "Vui lòng chọn biến thể sản phẩm (size/màu)")
    private Integer variantId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
}
