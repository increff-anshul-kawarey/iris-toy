package com.iris.increff.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Entity
@Table(name = "styles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Style {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "style_code", length = 50, nullable = false, unique = true)
    @NotNull(message = "Style code cannot be null")
    @Size(min = 1, max = 50, message = "Style code must be between 1 and 50 characters")
    private String styleCode;

    @Column(name = "brand", length = 50, nullable = false)
    @NotNull(message = "Brand cannot be null")
    @Size(min = 1, max = 50, message = "Brand must be between 1 and 50 characters")
    private String brand;

    @Column(name = "category", length = 50, nullable = false)
    @NotNull(message = "Category cannot be null")
    @Size(min = 1, max = 50, message = "Category must be between 1 and 50 characters")
    private String category;

    @Column(name = "sub_category", length = 50, nullable = false)
    @NotNull(message = "Sub-category cannot be null")
    @Size(min = 1, max = 50, message = "Sub-category must be between 1 and 50 characters")
    private String subCategory;

    @Column(name = "mrp", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "MRP cannot be null")
    @DecimalMin(value = "0.01", message = "MRP must be greater than 0")
    private BigDecimal mrp;

    @Column(name = "gender", length = 50, nullable = false)
    @NotNull(message = "Gender cannot be null")
    @Size(min = 1, max = 50, message = "Gender must be between 1 and 50 characters")
    private String gender;
}
