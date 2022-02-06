package com.iris.increff.controller;

import com.iris.increff.util.ProcessTsv;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@Api
@RestController
public class FileController {


    @ApiOperation(value = "Upload File Tsv")
    @RequestMapping(value = "/api/file/upload/{fileName}", method = RequestMethod.POST)
    public void uploadFileTsv(@PathVariable String fileName, @RequestPart("file") MultipartFile file) throws InterruptedException {
        System.out.println("File Is Uploaded Successfully :- " + fileName);

    }

    @ApiOperation(value = "Download Input for File ")
    @RequestMapping(path = "/api/file/input/{fileName}", method = RequestMethod.GET)
    public void exportInputTSV(@PathVariable String fileName, HttpServletResponse response) throws IOException, InterruptedException {
        ProcessTsv.createFileResponse(new File("src/main/java/com/iris/increff/Files/fileInput.tsv"), response);
        System.out.println("Export Input File Download is Successful :- " + fileName);
    }

    @ApiOperation(value = "Download template for File")
    @RequestMapping(path = "/api/file/template/{fileName}", method = RequestMethod.GET)
    public void exportTemplateTSV(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        ProcessTsv.createFileResponse(new File("src/main/java/com/iris/increff/Files/fileTemplate.tsv"), response);
        System.out.println("Download Input Template File is Successful :- " + fileName);

    }

    @ApiOperation(value = "Download Errors for input")
    @RequestMapping(path = "/api/file/errors/{fileName}", method = RequestMethod.GET)
    public void exportErrorTSV(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        ProcessTsv.createFileResponse(new File("src/main/java/com/iris/increff/Files/fileError.tsv"), response);
        System.out.println("Download Validation Error for File is Successful :- " + fileName);

    }

}
