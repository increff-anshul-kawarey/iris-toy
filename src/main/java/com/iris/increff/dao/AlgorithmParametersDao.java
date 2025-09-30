package com.iris.increff.dao;

import com.iris.increff.model.AlgorithmParameters;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * DAO for Algorithm Parameters
 *
 * Handles persistence of configurable algorithm parameters.
 *
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
@Repository
public class AlgorithmParametersDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Save or update algorithm parameters
     */
    @Transactional
    public AlgorithmParameters save(AlgorithmParameters parameters) {
        if (parameters.getId() == null) {
            entityManager.persist(parameters);
        } else {
            entityManager.merge(parameters);
        }
        return parameters;
    }

    /**
     * Find by parameter set name
     */
    public AlgorithmParameters findByParameterSet(String parameterSet) {
        TypedQuery<AlgorithmParameters> query = entityManager.createQuery(
            "SELECT a FROM AlgorithmParameters a WHERE a.parameterSet = :parameterSet AND a.isActive = true",
            AlgorithmParameters.class
        );
        query.setParameter("parameterSet", parameterSet);

        List<AlgorithmParameters> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Get all active parameter sets
     */
    public List<AlgorithmParameters> findAllActive() {
        TypedQuery<AlgorithmParameters> query = entityManager.createQuery(
            "SELECT a FROM AlgorithmParameters a WHERE a.isActive = true ORDER BY a.lastUpdatedDate DESC",
            AlgorithmParameters.class
        );
        return query.getResultList();
    }

    /**
     * Get default parameters (fallback if no parameter set exists)
     */
    @Transactional
    public AlgorithmParameters getDefaultParameters() {
        try {
            AlgorithmParameters defaultParams = findByParameterSet("default");

            if (defaultParams == null) {
                // Create default parameters if they don't exist
                defaultParams = new AlgorithmParameters();
                defaultParams.setParameterSet("default");
                defaultParams.setLiquidationThreshold(0.25);
                defaultParams.setBestsellerMultiplier(1.20);
                defaultParams.setMinVolumeThreshold(25.0);
                defaultParams.setConsistencyThreshold(0.75);
                defaultParams.setDescription("Default NOOS parameters");
                defaultParams.setCoreDurationMonths(6);
                defaultParams.setBestsellerDurationDays(90);
                defaultParams.setIsActive(true);

                // Set reasonable date range based on data
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    defaultParams.setAnalysisStartDate(sdf.parse("2019-01-01"));
                    defaultParams.setAnalysisEndDate(sdf.parse("2019-06-23"));
                } catch (Exception e) {
                    // Use current date as fallback
                    defaultParams.setAnalysisStartDate(new java.util.Date());
                    defaultParams.setAnalysisEndDate(new java.util.Date());
                }

                // Persist and flush to ensure it's saved immediately
                entityManager.persist(defaultParams);
                entityManager.flush();
                
                System.out.println("✅ Created default algorithm parameters in database");
            }

            return defaultParams;
        } catch (Exception e) {
            System.err.println("❌ Error in getDefaultParameters: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get default parameters", e);
        }
    }

    /**
     * Soft delete parameter set
     */
    @Transactional
    public void deactivateParameterSet(String parameterSet) {
        AlgorithmParameters params = findByParameterSet(parameterSet);
        if (params != null) {
            params.setIsActive(false);
            entityManager.merge(params);
        }
    }
}
