package com.iris.increff.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * NOOS Algorithm Result Entity
 * 
 * Stores the output of NOOS classification as specified in PRD:
 * | Category | Style Code | Style ROS | Type | Style Rev Contri |
 * 
 * PRD Compliance:
 * - Matches exact output format required
 * - Stores algorithm results for TSV download
 * - Tracks when results were calculated
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
@Entity
@Table(name = "noos_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoosResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category", length = 50, nullable = false)
    @NotNull(message = "Category cannot be null")
    private String category;

    @Column(name = "style_code", length = 50, nullable = false)
    @NotNull(message = "Style code cannot be null")
    private String styleCode;

    @Column(name = "style_ros", nullable = false, precision = 10, scale = 4)
    @NotNull(message = "Style ROS cannot be null")
    private BigDecimal styleROS;

    @Column(name = "type", length = 20, nullable = false)
    @NotNull(message = "Type cannot be null")
    private String type; // "core", "bestseller", "fashion"

    @Column(name = "style_rev_contribution", nullable = false, precision = 10, scale = 4)
    @NotNull(message = "Style revenue contribution cannot be null")
    private BigDecimal styleRevContribution;

    @Column(name = "calculated_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(message = "Calculated date cannot be null")
    private Date calculatedDate;

    // Additional fields for analysis and debugging
    @Column(name = "total_quantity_sold")
    private Integer totalQuantitySold;

    @Column(name = "total_revenue", precision = 15, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "days_available")
    private Integer daysAvailable;

    @Column(name = "days_with_sales")
    private Integer daysWithSales;

    @Column(name = "avg_discount", precision = 10, scale = 4)
    private BigDecimal avgDiscount;

    @Column(name = "algorithm_run_id")
    private Long algorithmRunId;
}
