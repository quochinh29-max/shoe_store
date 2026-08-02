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
public class ProductVariantDTO {

    private Integer id;
    private String size;
    private String color;
    private BigDecimal price;
    private Integer stockQuantity;
}
