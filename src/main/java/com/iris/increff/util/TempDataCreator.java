package com.iris.increff.util;

import com.iris.increff.model.AlgoParametersData;
import com.iris.increff.model.DashBoardData;
import com.iris.increff.model.Report1Data;
import com.iris.increff.model.Report2Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TempDataCreator {

    public static List<Report1Data> createReport1Data(int n) {
        List<Report1Data> reportData = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            reportData.add(getReport1Data(i));
        }
        return reportData;

    }

    private static Report1Data getReport1Data(int i) {
        Report1Data data = new Report1Data();
        data.setField1("Field1-" + i);
        data.setField2("Field2-" + i);
        data.setField3("Field3-" + i);
        data.setField4(i);
        return data;
    }

    public static List<Report2Data> createReport2Data(int n) {
        List<Report2Data> reportData = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            reportData.add(getReport2Data(i));
        }
        return reportData;

    }

    private static Report2Data getReport2Data(int i) {
        Report2Data data = new Report2Data();
        data.setField1(i);
        data.setField1(i);
        data.setField3("Field3-" + i);
        data.setField4(i / 1.00);
        data.setField5(i / 1.00);
        return data;
    }

    public static AlgoParametersData getAlgoParameters() {
        AlgoParametersData algoParametersData = new AlgoParametersData();

        // NOOS Algorithm Parameters (calibrated for synthetic data)
        algoParametersData.setParameter1(0.25);  // Liquidation threshold: 25% discount
        algoParametersData.setParameter2(1.20);  // Bestseller multiplier: 1.2x category average
        algoParametersData.setParameter3(25.00); // Min volume: 25 units
        algoParametersData.setParameter4(0.75);  // Consistency threshold: 75%
        algoParametersData.setParameter5("default_config");

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

    public static DashBoardData createDashBoardData() {
        DashBoardData dashBoardData = new DashBoardData();
        dashBoardData.setDashBoardTile1(1);
        dashBoardData.setDashBoardTile2(2);
        dashBoardData.setDashBoardTile3(3);
        dashBoardData.setDashBoardTile4(4);
        dashBoardData.setDashBoardTile1Msg("ONE");
        dashBoardData.setDashBoardTile2Msg("TWO");
        dashBoardData.setDashBoardTile3Msg("THREE");
        dashBoardData.setDashBoardTile4Msg("FOUR");
        return dashBoardData;

    }
}
