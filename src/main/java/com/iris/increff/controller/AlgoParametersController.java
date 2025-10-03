package com.iris.increff.controller;

import com.iris.increff.model.AlgoParametersData;
import com.iris.increff.service.AlgorithmParametersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api
@RestController
public class AlgoParametersController {

    @Autowired
    private AlgorithmParametersService algorithmParametersService;

    @ApiOperation(value = "Gets all active parameter sets (DTO)")
    @RequestMapping(path = "/api/algo/sets", method = RequestMethod.GET)
    public List<AlgoParametersData> getAllActiveSets() {
        return algorithmParametersService.getActiveParameterSetsData();
    }

    @ApiOperation(value = "Gets recent parameter sets (active+inactive), default 10")
    @RequestMapping(path = "/api/algo/sets/recent", method = RequestMethod.GET)
    public List<AlgoParametersData> getRecentSets(@RequestParam(defaultValue = "10") int limit) {
        if (limit < 1) limit = 1;
        if (limit > 100) limit = 100;
        return algorithmParametersService.getRecentParameterSetsData(limit);
    }

    @ApiOperation(value = "Gets a single AlgoParameter by its name (DTO)")
    @RequestMapping(path = "/api/algo/set/{parameterSet}", method = RequestMethod.GET)
    public AlgoParametersData getSet(@PathVariable String parameterSet) {
        return algorithmParametersService.getParameterSetData(parameterSet);
    }

    @ApiOperation(value = "Get current active parameters (DTO)")
    @RequestMapping(path = "/api/algo/current", method = RequestMethod.GET)
    public AlgoParametersData getCurrent() {
        return algorithmParametersService.getCurrentParameters();
    }

    @ApiOperation(value = "Get default parameters (DTO)")
    @RequestMapping(path = "/api/algo/defaults", method = RequestMethod.GET)
    public AlgoParametersData getDefaults() {
        return algorithmParametersService.getDefaultParametersAsData();
    }

    @ApiOperation(value = "Update current active parameters (DTO)")
    @RequestMapping(path = "/api/algo/update", method = RequestMethod.POST)
    public AlgoParametersData updateCurrent(@RequestBody AlgoParametersData data) {
        return algorithmParametersService.updateCurrentParameters(data);
    }

    @ApiOperation(value = "Create a new parameter set and activate it")
    @RequestMapping(path = "/api/algo/create", method = RequestMethod.POST)
    public AlgoParametersData createNew(@RequestBody AlgoParametersData data, @RequestParam(required = false) String name) {
        return algorithmParametersService.createNewParameterSet(data, name);
    }

    // ===== Backward-compatible endpoints for legacy UI (algoProperties.js) =====
    @ApiOperation(value = "[Legacy] Get algorithm parameters in parameter1..5 format")
    @RequestMapping(path = "/api/algo", method = RequestMethod.GET)
    public java.util.Map<String, Object> getLegacy() {
        AlgoParametersData current = algorithmParametersService.getCurrentParameters();
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        if (current != null) {
            map.put("parameter1", current.getLiquidationThreshold());
            map.put("parameter2", current.getBestsellerMultiplier());
            map.put("parameter3", current.getMinVolumeThreshold());
            map.put("parameter4", current.getConsistencyThreshold());
            map.put("parameter5", current.getAlgorithmLabel());
        }
        return map;
    }

    @ApiOperation(value = "[Legacy] Update algorithm parameters from parameter1..5 format")
    @RequestMapping(path = "/api/algo", method = RequestMethod.PUT)
    public java.util.Map<String, Object> updateLegacy(@RequestBody java.util.Map<String, Object> legacy) {
        // Map legacy fields to DTO
        AlgoParametersData dto = new AlgoParametersData();
        // Safely parse numbers; fallback to current if missing
        AlgoParametersData current = algorithmParametersService.getCurrentParameters();
        dto.setLiquidationThreshold(parseDouble(legacy.get("parameter1"), current != null ? current.getLiquidationThreshold() : 0.25));
        dto.setBestsellerMultiplier(parseDouble(legacy.get("parameter2"), current != null ? current.getBestsellerMultiplier() : 1.2));
        dto.setMinVolumeThreshold(parseDouble(legacy.get("parameter3"), current != null ? current.getMinVolumeThreshold() : 25.0));
        dto.setConsistencyThreshold(parseDouble(legacy.get("parameter4"), current != null ? current.getConsistencyThreshold() : 0.75));
        dto.setAlgorithmLabel(asString(legacy.get("parameter5"), current != null ? current.getAlgorithmLabel() : "Default NOOS parameters"));

        AlgoParametersData updated = algorithmParametersService.updateCurrentParameters(dto);

        // Return legacy shape for compatibility
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("parameter1", updated.getLiquidationThreshold());
        map.put("parameter2", updated.getBestsellerMultiplier());
        map.put("parameter3", updated.getMinVolumeThreshold());
        map.put("parameter4", updated.getConsistencyThreshold());
        map.put("parameter5", updated.getAlgorithmLabel());
        return map;
    }

    private static double parseDouble(Object value, double fallback) {
        if (value == null) return fallback;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return Double.parseDouble(String.valueOf(value)); } catch (Exception e) { return fallback; }
    }

    private static String asString(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }
}
