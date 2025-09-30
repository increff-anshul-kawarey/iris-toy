package com.iris.increff.model;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

/**
 * System Health Report Data Model
 * 
 * Contains system health metrics including upload statistics and task performance
 * 
 * @author Anshuk Kawarry
 * @version 2.0
 * @since 2025-01-01
 */
@Getter
@Setter
public class Report2Data {

    // Task and upload metrics
    private Date date;
    private String taskType;
    private Integer totalTasks;
    private Integer successfulTasks;
    private Integer failedTasks;
    private Double successRate;
    
    // System performance
    private Double averageExecutionTime;
    private String systemStatus;

    /**
     * Constructor for system health data
     */
    public Report2Data(Date date, String taskType, Integer totalTasks, Integer successfulTasks,
                      Integer failedTasks, Double successRate, Double averageExecutionTime, String systemStatus) {
        this.date = date;
        this.taskType = taskType;
        this.totalTasks = totalTasks;
        this.successfulTasks = successfulTasks;
        this.failedTasks = failedTasks;
        this.successRate = successRate;
        this.averageExecutionTime = averageExecutionTime;
        this.systemStatus = systemStatus;
    }

    /**
     * Default constructor
     */
    public Report2Data() {
    }
}
