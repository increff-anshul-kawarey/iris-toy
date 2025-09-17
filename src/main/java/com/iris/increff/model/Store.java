package com.iris.increff.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "stores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "branch", length = 50, nullable = false)
    @NotNull(message = "Branch cannot be null")
    @Size(min = 1, max = 50, message = "Branch must be between 1 and 50 characters")
    private String branch;

    @Column(name = "city", length = 50, nullable = false)
    @NotNull(message = "City cannot be null")
    @Size(min = 1, max = 50, message = "City must be between 1 and 50 characters")
    private String city;
}
