package com.example.shoestore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(
        regexp = "PENDING|CONFIRMED|SHIPPING|COMPLETED|CANCELLED",
        message = "Trạng thái không hợp lệ"
    )
    private String orderStatus;
}
