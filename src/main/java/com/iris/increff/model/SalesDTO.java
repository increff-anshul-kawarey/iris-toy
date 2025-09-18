package com.iris.increff.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesDTO {

    private Integer id;

    @NotNull(message = "Date cannot be null")
    private Date date;

    @NotNull(message = "SKU ID cannot be null")
    private Integer skuId;

    @NotNull(message = "Store ID cannot be null")
    private Integer storeId;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "Discount cannot be null")
    @DecimalMin(value = "0.00", message = "Discount must be greater than or equal to 0")
    private BigDecimal discount;

    @NotNull(message = "Revenue cannot be null")
    @DecimalMin(value = "0.01", message = "Revenue must be positive")
    private BigDecimal revenue;
}
