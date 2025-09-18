package com.iris.increff.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SKUDTO {

    private Integer id;

    @NotNull(message = "SKU cannot be null")
    @Size(min = 1, max = 50, message = "SKU must be between 1 and 50 characters")
    private String sku;

    @NotNull(message = "Style ID cannot be null")
    private Integer styleId;

    @NotNull(message = "Size cannot be null")
    @Size(min = 1, max = 10, message = "Size must be between 1 and 10 characters")
    private String size;
}
