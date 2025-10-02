package com.iris.increff.util;

import com.iris.increff.model.AlgoParametersData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TempDataCreator {

    public static AlgoParametersData getAlgoParameters() {
        AlgoParametersData algoParametersData = new AlgoParametersData();

        // NOOS Algorithm Parameters (calibrated for synthetic data)
        algoParametersData.setLiquidationThreshold(0.25);  // Liquidation threshold: 25% discount
        algoParametersData.setBestsellerMultiplier(1.20);  // Bestseller multiplier: 1.2x category average
        algoParametersData.setMinVolumeThreshold(25.00);   // Min volume: 25 units
        algoParametersData.setConsistencyThreshold(0.75);  // Consistency threshold: 75%
        algoParametersData.setAlgorithmLabel("default_config");

        // Date Analysis Parameters (based on dataset: 2018-12-31 to 2019-06-23)
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            algoParametersData.setAnalysisStartDate(dateFormat.parse("2019-01-01"));
            algoParametersData.setAnalysisEndDate(dateFormat.parse("2019-06-23"));
            algoParametersData.setCoreDurationMonths(6);      // Full dataset period
            algoParametersData.setBestsellerDurationDays(90); // Last 3 months
        } catch (ParseException e) {
            // Fallback if date parsing fails
            algoParametersData.setAnalysisStartDate(new Date());
            algoParametersData.setAnalysisEndDate(new Date());
            algoParametersData.setCoreDurationMonths(3);
            algoParametersData.setBestsellerDurationDays(30);
        }

        return algoParametersData;
    }
}
