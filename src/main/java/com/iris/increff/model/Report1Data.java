package com.iris.increff.model;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

/**
 * NOOS Analytics Report Data Model
 * 
 * Contains NOOS algorithm execution history and classification insights
 * 
 * @author Anshuk Kawarry
 * @version 2.0
 * @since 2025-01-01
 */
@Getter
@Setter
public class Report1Data {

    // Algorithm execution details
    private Date executionDate;
    private String algorithmLabel;
    private String executionStatus;
    private Integer totalStylesProcessed;
    
    // Classification results
    private Integer coreStyles;
    private Integer bestsellerStyles;
    private Integer fashionStyles;
    
    // Performance metrics
    private Double executionTimeMinutes;
    private String parameters;

    /**
     * Constructor for NOOS analytics data
     */
    public Report1Data(Date executionDate, String algorithmLabel, String executionStatus,
                      Integer totalStylesProcessed, Integer coreStyles, Integer bestsellerStyles,
                      Integer fashionStyles, Double executionTimeMinutes, String parameters) {
        this.executionDate = executionDate;
        this.algorithmLabel = algorithmLabel;
        this.executionStatus = executionStatus;
        this.totalStylesProcessed = totalStylesProcessed;
        this.coreStyles = coreStyles;
        this.bestsellerStyles = bestsellerStyles;
        this.fashionStyles = fashionStyles;
        this.executionTimeMinutes = executionTimeMinutes;
        this.parameters = parameters;
    }

    /**
     * Default constructor
     */
    public Report1Data() {
    }
}
