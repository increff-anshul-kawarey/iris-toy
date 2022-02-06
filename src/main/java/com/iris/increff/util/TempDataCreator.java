package com.iris.increff.util;

import com.iris.increff.model.AlgoParametersData;
import com.iris.increff.model.DashBoardData;
import com.iris.increff.model.Report1Data;
import com.iris.increff.model.Report2Data;

import java.util.ArrayList;
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
        algoParametersData.setParameter1(1.00);
        algoParametersData.setParameter2(2.00);
        algoParametersData.setParameter3(3.00);
        algoParametersData.setParameter4(4.00);
        algoParametersData.setParameter5("22-01-2022");
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
