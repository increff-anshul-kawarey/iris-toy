package com.iris.increff.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AlgoParametersData {
    // NOOS Algorithm Parameters
    private double parameter1;  // Liquidation threshold (e.g., 0.20 = 20% discount)
    private double parameter2;  // Bestseller multiplier (e.g., 1.5 = 150% of category average)
    private double parameter3;  // Min volume threshold (e.g., 25 units)
    private double parameter4;  // Consistency threshold (e.g., 0.75 = 75% selling days)
    private String parameter5;  // Label/description

    // Date Analysis Parameters (PRD Requirement)
    private Date analysisStartDate;  // Start date for sales analysis
    private Date analysisEndDate;    // End date for sales analysis
    private Integer coreDurationMonths;     // Core analysis duration in months
    private Integer bestsellerDurationDays; // Bestseller analysis duration in days
}
