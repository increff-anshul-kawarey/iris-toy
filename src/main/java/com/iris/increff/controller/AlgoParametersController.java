package com.iris.increff.controller;

import com.iris.increff.dao.AlgorithmParametersDao;
import com.iris.increff.model.AlgorithmParameters;
import com.iris.increff.model.AlgoParametersData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Api
@RestController
public class AlgoParametersController {

    private static final Logger logger = LoggerFactory.getLogger(AlgoParametersController.class);

    @Autowired
    private AlgorithmParametersDao algorithmParametersDao;

    @ApiOperation(value = "Get current algorithm parameters")
    @RequestMapping(path = "/api/algo/current", method = RequestMethod.GET)
    @Transactional
    public ResponseEntity<AlgoParametersData> getCurrentParameters() {
        try {
            logger.info("🔄 Getting current algorithm parameters...");
            AlgorithmParameters params = algorithmParametersDao.getDefaultParameters();
            
            if (params == null) {
                logger.error("❌ No default parameters found and failed to create them");
                return ResponseEntity.status(500).build();
            }
            
            AlgoParametersData data = params.toAlgoParametersData();
            logger.info("✅ Retrieved algorithm parameters: {}", params.getParameterSet());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("❌ Failed to retrieve algorithm parameters: {}", e.getMessage(), e);
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @ApiOperation(value = "Update algorithm parameters")
    @RequestMapping(path = "/api/algo/update", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<AlgoParametersData> updateParameters(@RequestBody AlgoParametersData algoParametersData) {
        try {
            logger.info("🔄 Updating algorithm parameters");

            // Get or create default parameter set
            AlgorithmParameters params = algorithmParametersDao.getDefaultParameters();

            // Update from request data
            params.updateFromAlgoParametersData(algoParametersData, "system");

            // Save to database
            algorithmParametersDao.save(params);

            logger.info("✅ Algorithm parameters updated successfully");
            return ResponseEntity.ok(params.toAlgoParametersData());

        } catch (Exception e) {
            logger.error("❌ Failed to update algorithm parameters: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @ApiOperation(value = "Get all parameter sets")
    @RequestMapping(path = "/api/algo/sets", method = RequestMethod.GET)
    @Transactional
    public ResponseEntity<List<AlgoParametersData>> getAllParameterSets() {
        try {
            logger.info("🔄 Getting all parameter sets...");
            
            // Ensure default parameters exist first
            algorithmParametersDao.getDefaultParameters();
            
            List<AlgorithmParameters> paramSets = algorithmParametersDao.findAllActive();
            List<AlgoParametersData> dataList = paramSets.stream()
                    .map(AlgorithmParameters::toAlgoParametersData)
                    .collect(Collectors.toList());

            logger.info("✅ Retrieved {} parameter sets", dataList.size());
            return ResponseEntity.ok(dataList);

        } catch (Exception e) {
            logger.error("❌ Failed to retrieve parameter sets: {}", e.getMessage(), e);
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @ApiOperation(value = "Create new parameter set")
    @RequestMapping(path = "/api/algo/sets", method = RequestMethod.POST)
    public ResponseEntity<AlgoParametersData> createParameterSet(
            @RequestBody AlgoParametersData algoParametersData,
            @RequestParam String parameterSetName) {

        try {
            logger.info("🔄 Creating new parameter set: {}", parameterSetName);

            // Check if parameter set already exists
            AlgorithmParameters existing = algorithmParametersDao.findByParameterSet(parameterSetName);
            if (existing != null) {
                logger.warn("⚠️ Parameter set '{}' already exists", parameterSetName);
                return ResponseEntity.badRequest().build();
            }

            // Create new parameter set
            AlgorithmParameters params = new AlgorithmParameters();
            params.setParameterSet(parameterSetName);
            params.setIsActive(true);
            params.updateFromAlgoParametersData(algoParametersData, "system");

            // Save to database
            algorithmParametersDao.save(params);

            logger.info("✅ Created new parameter set: {}", parameterSetName);
            return ResponseEntity.ok(params.toAlgoParametersData());

        } catch (Exception e) {
            logger.error("❌ Failed to create parameter set: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @ApiOperation(value = "Get specific parameter set")
    @RequestMapping(path = "/api/algo/sets/{parameterSetName}", method = RequestMethod.GET)
    public ResponseEntity<AlgoParametersData> getParameterSet(@PathVariable String parameterSetName) {
        try {
            AlgorithmParameters params = algorithmParametersDao.findByParameterSet(parameterSetName);
            if (params == null) {
                logger.warn("⚠️ Parameter set '{}' not found", parameterSetName);
                return ResponseEntity.notFound().build();
            }

            logger.info("✅ Retrieved parameter set: {}", parameterSetName);
            return ResponseEntity.ok(params.toAlgoParametersData());

        } catch (Exception e) {
            logger.error("❌ Failed to retrieve parameter set '{}': {}", parameterSetName, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @ApiOperation(value = "Get default parameters")
    @RequestMapping(path = "/api/algo/defaults", method = RequestMethod.GET)
    public ResponseEntity<AlgoParametersData> getDefaultParameters() {
        try {
            logger.info("🔄 Loading default parameters");
            
            // Create fresh default parameters (not from database)
            AlgorithmParameters defaults = new AlgorithmParameters();
            defaults.setParameterSet("default");
            defaults.setLiquidationThreshold(0.25);
            defaults.setBestsellerMultiplier(1.20);
            defaults.setMinVolumeThreshold(25.0);
            defaults.setConsistencyThreshold(0.75);
            defaults.setDescription("Default NOOS parameters");
            defaults.setCoreDurationMonths(6);
            defaults.setBestsellerDurationDays(90);
            
            // Set reasonable date range
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                defaults.setAnalysisStartDate(sdf.parse("2019-01-01"));
                defaults.setAnalysisEndDate(sdf.parse("2019-06-23"));
            } catch (Exception e) {
                defaults.setAnalysisStartDate(new java.util.Date());
                defaults.setAnalysisEndDate(new java.util.Date());
            }

            logger.info("✅ Default parameters loaded");
            return ResponseEntity.ok(defaults.toAlgoParametersData());

        } catch (Exception e) {
            logger.error("❌ Failed to load default parameters: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @ApiOperation(value = "Get current algorithm parameters (legacy endpoint)")
    @RequestMapping(path = "/api/algo", method = RequestMethod.GET)
    public ResponseEntity<AlgoParametersData> getCurrentParametersLegacy() {
        // Legacy endpoint for dashboard compatibility
        return getCurrentParameters();
    }

    @ApiOperation(value = "Update algorithm parameters (legacy endpoint)")
    @RequestMapping(path = "/api/algo", method = RequestMethod.PUT)
    @Transactional
    public ResponseEntity<AlgoParametersData> updateParametersLegacy(@RequestBody AlgoParametersData algoParametersData) {
        // Legacy endpoint for dashboard compatibility
        return updateParameters(algoParametersData);
    }

    @ApiOperation(value = "Reset to default parameters")
    @RequestMapping(path = "/api/algo/reset", method = RequestMethod.POST)
    public ResponseEntity<AlgoParametersData> resetToDefaults() {
        try {
            logger.info("🔄 Resetting parameters to defaults");

            // Delete existing default parameters (they will be recreated)
            AlgorithmParameters existing = algorithmParametersDao.findByParameterSet("default");
            if (existing != null) {
                algorithmParametersDao.deactivateParameterSet("default");
            }

            // Get new defaults (will be created automatically)
            AlgorithmParameters defaults = algorithmParametersDao.getDefaultParameters();

            logger.info("✅ Parameters reset to defaults");
            return ResponseEntity.ok(defaults.toAlgoParametersData());

        } catch (Exception e) {
            logger.error("❌ Failed to reset parameters: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
