package com.iris.increff.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StyleDTO {

    private Integer id;

    @NotNull(message = "Style code cannot be null")
    @Size(min = 1, max = 50, message = "Style code must be between 1 and 50 characters")
    private String styleCode;

    @NotNull(message = "Brand cannot be null")
    @Size(min = 1, max = 50, message = "Brand must be between 1 and 50 characters")
    private String brand;

    @NotNull(message = "Category cannot be null")
    @Size(min = 1, max = 50, message = "Category must be between 1 and 50 characters")
    private String category;

    @NotNull(message = "Sub-category cannot be null")
    @Size(min = 1, max = 50, message = "Sub-category must be between 1 and 50 characters")
    private String subCategory;

    @DecimalMin(value = "0.01", message = "MRP must be greater than 0")
    private BigDecimal mrp;

    @NotNull(message = "Gender cannot be null")
    @Size(min = 1, max = 50, message = "Gender must be between 1 and 50 characters")
    private String gender;
}