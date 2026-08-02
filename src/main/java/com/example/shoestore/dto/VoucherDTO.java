package com.example.shoestore.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDTO {

    private Integer id;

    @NotBlank(message = "Mã voucher không được để trống")
    @Size(max = 50, message = "Mã voucher tối đa 50 ký tự")
    private String code;

    @NotBlank(message = "Loại giảm giá không được để trống")
    @Pattern(regexp = "PERCENT|FIXED_AMOUNT", message = "Loại giảm giá phải là PERCENT hoặc FIXED_AMOUNT")
    private String discountType;

    @NotNull(message = "Giá trị giảm giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm giá phải lớn hơn 0")
    private BigDecimal discountValue;

    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    private BigDecimal maxDiscount;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;

    private Integer usageLimit;

    @Builder.Default
    private Integer usedCount = 0;

    /**
     * Trạng thái tính toán tại thời điểm trả về, không lưu trong DB:
     * ACTIVE / UPCOMING / EXPIRED / OUT_OF_USES
     */
    private String status;
}
