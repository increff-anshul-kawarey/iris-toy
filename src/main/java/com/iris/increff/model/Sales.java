package com.iris.increff.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date", nullable = false)
    @NotNull(message = "Date cannot be null")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "sku_id", nullable = false)
    @NotNull(message = "SKU ID cannot be null")
    private Integer skuId;

    @Column(name = "store_id", nullable = false)
    @NotNull(message = "Store ID cannot be null")
    private Integer storeId;

    @Column(name = "quantity", nullable = false)
    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be positive")
    private Integer quantity;

    @Column(name = "discount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Discount cannot be null")
    @DecimalMin(value = "0.00", message = "Discount must be greater than or equal to 0")
    private BigDecimal discount;

    @Column(name = "revenue", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Revenue cannot be null")
    @DecimalMin(value = "0.01", message = "Revenue must be positive")
    private BigDecimal revenue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", insertable = false, updatable = false)
    private SKU sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;
}
