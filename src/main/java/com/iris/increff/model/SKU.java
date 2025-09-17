package com.iris.increff.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "skus")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SKU {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sku", length = 50, nullable = false, unique = true)
    @NotNull(message = "SKU cannot be null")
    @Size(min = 1, max = 50, message = "SKU must be between 1 and 50 characters")
    private String sku;

    @Column(name = "style_id", nullable = false)
    @NotNull(message = "Style ID cannot be null")
    private Integer styleId;

    @Column(name = "size", length = 10, nullable = false)
    @NotNull(message = "Size cannot be null")
    @Size(min = 1, max = 10, message = "Size must be between 1 and 10 characters")
    private String size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "style_id", insertable = false, updatable = false)
    private Style style;
}
