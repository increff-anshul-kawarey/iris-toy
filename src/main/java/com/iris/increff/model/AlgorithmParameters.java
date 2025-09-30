package com.iris.increff.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Algorithm Parameters Entity
 *
 * PRD Requirement: "Algorithm parameters should be editable in the UI"
 *
 * Stores configurable parameters for algorithms like NOOS.
 * Supports persistence of parameter sets with versioning.
 *
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
@Entity
@Table(name = "algorithm_parameters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlgorithmParameters {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "parameter_set", unique = true)
    private String parameterSet; // "default", "seasonal", "test", etc.

    // NOOS Algorithm Parameters
    @Column(name = "liquidation_threshold", nullable = false)
    private Double liquidationThreshold = 0.25;  // parameter1

    @Column(name = "bestseller_multiplier", nullable = false)
    private Double bestsellerMultiplier = 1.20;  // parameter2

    @Column(name = "min_volume_threshold", nullable = false)
    private Double minVolumeThreshold = 25.0;    // parameter3

    @Column(name = "consistency_threshold", nullable = false)
    private Double consistencyThreshold = 0.75;  // parameter4

    @Column(name = "description")
    private String description;                   // parameter5

    // Date Analysis Parameters
    @Column(name = "analysis_start_date")
    private Date analysisStartDate;

    @Column(name = "analysis_end_date")
    private Date analysisEndDate;

    @Column(name = "core_duration_months")
    private Integer coreDurationMonths = 6;

    @Column(name = "bestseller_duration_days")
    private Integer bestsellerDurationDays = 90;

    // Metadata
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_date", nullable = false)
    private Date createdDate;

    @Column(name = "last_updated_date")
    private Date lastUpdatedDate;

    @Column(name = "updated_by")
    private String updatedBy;

    // Pre-persist hook to set timestamps
    @PrePersist
    protected void onCreate() {
        createdDate = new Date();
        lastUpdatedDate = createdDate;
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedDate = new Date();
    }

    /**
     * Convert to AlgoParametersData for API responses
     */
    public AlgoParametersData toAlgoParametersData() {
        AlgoParametersData data = new AlgoParametersData();
        
        // Parameter Set Information
        data.setParameterSetName(this.parameterSet);
        data.setIsActive(this.isActive);
        data.setLastUpdated(this.lastUpdatedDate);
        
        // Algorithm Parameters
        data.setLiquidationThreshold(this.liquidationThreshold);
        data.setBestsellerMultiplier(this.bestsellerMultiplier);
        data.setMinVolumeThreshold(this.minVolumeThreshold);
        data.setConsistencyThreshold(this.consistencyThreshold);
        data.setAlgorithmLabel(this.description);
        data.setAnalysisStartDate(this.analysisStartDate);
        data.setAnalysisEndDate(this.analysisEndDate);
        data.setCoreDurationMonths(this.coreDurationMonths);
        data.setBestsellerDurationDays(this.bestsellerDurationDays);
        return data;
    }

    /**
     * Update from AlgoParametersData
     */
    public void updateFromAlgoParametersData(AlgoParametersData data, String updatedBy) {
        this.liquidationThreshold = data.getLiquidationThreshold();
        this.bestsellerMultiplier = data.getBestsellerMultiplier();
        this.minVolumeThreshold = data.getMinVolumeThreshold();
        this.consistencyThreshold = data.getConsistencyThreshold();
        this.description = data.getAlgorithmLabel();
        this.analysisStartDate = data.getAnalysisStartDate();
        this.analysisEndDate = data.getAnalysisEndDate();
        this.coreDurationMonths = data.getCoreDurationMonths();
        this.bestsellerDurationDays = data.getBestsellerDurationDays();
        this.updatedBy = updatedBy;
    }
}
