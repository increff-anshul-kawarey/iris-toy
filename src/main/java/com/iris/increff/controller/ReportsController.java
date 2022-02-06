package com.iris.increff.controller;

import com.iris.increff.model.Report1Data;
import com.iris.increff.model.Report2Data;
import com.iris.increff.util.ProcessTsv;
import com.iris.increff.util.TempDataCreator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Api
@RestController
public class ReportsController {


    @ApiOperation(value = "Get Reports1")
    @RequestMapping(path = "/api/report/report1", method = RequestMethod.GET)
    public List<Report1Data> getReport1() {
        System.out.println("Fetch Report 1 is Successful");
        return TempDataCreator.createReport1Data(5);
    }

    @ApiOperation(value = "Get Reports2")
    @RequestMapping(path = "/api/report/report2", method = RequestMethod.GET)
    public List<Report2Data> getReport2() {
        System.out.println("Fetch Report 2 is Successful");
        return TempDataCreator.createReport2Data(10);
    }

    @ApiOperation(value = "Download Reports")
    @RequestMapping(path = "/api/report/download/{reportName}", method = RequestMethod.GET)
    public void getDownloadReport2(@PathVariable String reportName, HttpServletResponse response) throws IOException {
        ProcessTsv.createFileResponse(new File("src/main/java/com/iris/increff/Files/fileInput.tsv"), response);
        System.out.println("Download Report is Successful :-" + reportName);
    }
}
