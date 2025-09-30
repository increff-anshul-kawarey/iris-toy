package com.iris.increff.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AlgoParametersData {
    // Parameter Set Information
    private String parameterSetName;        // Name/identifier for this parameter set
    private Boolean isActive;               // Whether this parameter set is currently active
    private Date lastUpdated;               // When this parameter set was last modified
    
    // NOOS Algorithm Parameters - Meaningful Names
    private double liquidationThreshold;    // Discount threshold for liquidation cleanup (e.g., 0.20 = 20%)
    private double bestsellerMultiplier;    // Revenue multiplier for bestseller classification (e.g., 1.5 = 150% of category avg)
    private double minVolumeThreshold;      // Minimum volume threshold for classification (e.g., 25 units)
    private double consistencyThreshold;    // Consistency threshold for core classification (e.g., 0.75 = 75% selling days)
    private String algorithmLabel;          // Algorithm run label/description

    // Date Analysis Parameters (PRD Requirement)
    private Date analysisStartDate;         // Start date for sales analysis
    private Date analysisEndDate;           // End date for sales analysis
    private Integer coreDurationMonths;     // Core analysis duration in months
    private Integer bestsellerDurationDays; // Bestseller analysis duration in days
}
