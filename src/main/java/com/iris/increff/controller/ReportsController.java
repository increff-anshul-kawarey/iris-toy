package com.iris.increff.controller;

import com.iris.increff.model.Report1Data;
import com.iris.increff.model.Report2Data;
import com.iris.increff.service.ReportAnalyticsService;
import com.iris.increff.util.ProcessTsv;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);

    @Autowired
    private ReportAnalyticsService reportAnalyticsService;


    @ApiOperation(value = "Get NOOS Analytics Report")
    @RequestMapping(path = "/api/report/report1", method = RequestMethod.GET)
    public List<Report1Data> getReport1() {
        logger.info("NOOS Analytics Report requested");
        try {
            return reportAnalyticsService.generateNoosAnalyticsReport();
        } catch (Exception e) {
            logger.error("Failed to generate NOOS analytics report, using fallback: {}", e.getMessage());
            return null;
        }
    }

    @ApiOperation(value = "Get System Health Report")
    @RequestMapping(path = "/api/report/report2", method = RequestMethod.GET)
    public List<Report2Data> getReport2() {
        logger.info("System Health Report requested");
        try {
            return reportAnalyticsService.generateSystemHealthReport();
        } catch (Exception e) {
            logger.error("Failed to generate system health report, using fallback: {}", e.getMessage());
            return null;
        }
    }

    @ApiOperation(value = "Download Reports")
    @RequestMapping(path = "/api/report/download/{reportName}", method = RequestMethod.GET)
    public void getDownloadReport2(@PathVariable String reportName, HttpServletResponse response) throws IOException {
        ProcessTsv.createFileResponse(new File("src/main/resources/Files/fileInput.tsv"), response);
        logger.info("Report download successful: {}", reportName);
    }
}
