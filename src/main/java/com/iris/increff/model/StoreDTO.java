package com.iris.increff.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDTO {

    private Integer id;

    @NotNull(message = "Branch cannot be null")
    @Size(min = 1, max = 50, message = "Branch must be between 1 and 50 characters")
    private String branch;

    @NotNull(message = "City cannot be null")
    @Size(min = 1, max = 50, message = "City must be between 1 and 50 characters")
    private String city;
}
