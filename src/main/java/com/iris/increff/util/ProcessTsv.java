package com.iris.increff.util;


import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ProcessTsv {

    public static String[] stylesHeaders = {"style", "brand", "category", "sub_category", "mrp", "gender"};
    public static String[] skuHeaders = {"sku", "style", "size"};
    public static String[] storeHeaders = {"branch", "city"};
    public static String[] salesHeaders = {"day", "sku", "channel", "quantity", "discount", "revenue"};
    public static String[] priceBucketHeaders = {"bucket_name", "min_value", "max_value"};


    public static boolean verifyHeader(String[] headersActual, String[] headersExpected) throws ApiException {
        if (!Arrays.equals(headersActual, headersExpected)) {
            String headerString = Arrays.toString(headersActual);
            String actualHeaderString = Arrays.toString(headersExpected);
            throw new ApiException("Headers for File does Not Match Expected Headers Headers for File " + headerString + "Headers Expected" + actualHeaderString);
        }
        return true;
    }

    public static ArrayList<HashMap<String, String>> processTsv(MultipartFile file, String[] headers) throws ApiException {
        ArrayList<HashMap<String, String>> rowMaps = new ArrayList<>();
        try {
            if (file.getOriginalFilename().split("\\.").length!=2||!file.getOriginalFilename().split("\\.")[1].equals("tsv")) {
                throw new ApiException("File is not of .tsv type");
            }
            InputStream inputStream = file.getInputStream();
            BufferedReader TSVReader = new BufferedReader(new InputStreamReader(inputStream));
            String[] headersActual = TSVReader.readLine().split("\t");
            ProcessTsv.verifyHeader(headersActual, headers);
            String row = null;
            int fileRowCount = 0;
            while ((row = TSVReader.readLine()) != null) {
                HashMap<String, String> rowMap = new HashMap<>();
                String[] rowContent = row.split("\t");
                if (rowContent.length == headers.length) {
                    for (int i = 0; i < rowContent.length; i++) {
                        rowMap.put(headers[i], rowContent[i]);
                    }
                }
                rowMaps.add(rowMap);
                fileRowCount = fileRowCount + 1;
                if (fileRowCount > 500001) {
                    throw new ApiException("File Row count is greater than 500000");
                }
            }
            System.out.println(fileRowCount);
        } catch (ApiException apiException) {
            throw apiException;
        } catch (Exception e) {
            throw new ApiException("Some Error occured while Reading Tsv");
        }
        return rowMaps;
    }

    public static ResponseEntity<byte[]> generateDataForTemplate(String[] headers, String filename) {
        StringBuilder head = new StringBuilder();
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < headers.length; i++) {
            head.append(headers[i]);
            body.append("Value of ").append(headers[i]);
            if (i < headers.length - 1) {
                head.append("\t");
                body.append("\t");
            }
        }
        String content = head + "\n" + body;
        byte[] output = content.getBytes();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("charset", "utf-8");
        responseHeaders.setContentType(MediaType.valueOf("text/html"));
        responseHeaders.setContentLength(output.length);
        responseHeaders.set("Content-disposition", "attachment; filename=" + filename);
        return new ResponseEntity<byte[]>(output, responseHeaders, HttpStatus.OK);
    }

    protected static String generateDataForError(List<String> errors) {
        String body = "Errors in TSV";
        for (String error : errors) {
            body = body + '\n' + error;
        }
        return body;
    }

    public static void createFileResponse(List<String> errors, String fileName, HttpServletResponse response) throws IOException {
        if (errors == null) {
            return;
        }
        File file = File.createTempFile("" + System.currentTimeMillis(), ".csv");
        FileWriter writer = new FileWriter(file);
        writer.write(generateDataForError(errors));
        writer.flush();
        writer.close();
        response.setContentType("application/csv");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        OutputStream os = null;
        os = response.getOutputStream();
        FileUtils.copyFile(file, response.getOutputStream());
        os.flush();
    }
    public static void createFileResponse(File file, HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            FileUtils.copyFile(file, response.getOutputStream());
            os.flush();
        } finally {
            closeQuietly(os);
        }
    }

    protected static void closeQuietly(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            // DO NOTHING
        } finally {
            // DO NOTHING
        }
    }


}