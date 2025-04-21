package com.brokerage.orderapp.dto;

import com.brokerage.orderapp.entity.OrderSide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Asset name must not be blank")
    private String assetName;

    @NotNull(message = "Order side is required")
    private OrderSide orderSide;

    @NotNull(message = "Size is required")
    @DecimalMin(value = "0.0000001", message = "Size must be greater than zero")
    private BigDecimal size;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0000001", message = "Price must be greater than zero")
    private BigDecimal price;
}
