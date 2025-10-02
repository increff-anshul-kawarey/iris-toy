package com.iris.increff.service;

import com.iris.increff.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@Service
public class FileProcessingService {

    public ArrayList<HashMap<String, String>> processTsv(MultipartFile file, String[] headers) throws ApiException {
        ArrayList<HashMap<String, String>> rowMaps = new ArrayList<>();
        try {
            if (file.getOriginalFilename().split("\\.").length!=2||!file.getOriginalFilename().split("\\.")[1].equals("tsv")) {
                throw new ApiException("File is not of .tsv type");
            }
            InputStream inputStream = file.getInputStream();
            BufferedReader TSVReader = new BufferedReader(new InputStreamReader(inputStream));
            String[] headersActual = TSVReader.readLine().split("\t");
            verifyHeader(headersActual, headers);
            String row = null;
            int fileRowCount = 0;
            while ((row = TSVReader.readLine()) != null) {
                HashMap<String, String> rowMap = new HashMap<>();
                String[] rowContent = row.split("\t");
                if (rowContent.length == headers.length) {
                    for (int i = 0; i < rowContent.length; i++) {
                        // Normalize data: trim and convert to lowercase
                        String normalizedValue = rowContent[i] != null ? rowContent[i].trim().toLowerCase() : "";
                        rowMap.put(headers[i], normalizedValue);
                    }
                }
                rowMaps.add(rowMap);
                fileRowCount = fileRowCount + 1;
                if (fileRowCount > 500001) {
                    throw new ApiException("File Row count is greater than 500000");
                }
            }
        } catch (ApiException apiException) {
            throw apiException;
        } catch (Exception e) {
            throw new ApiException("Some Error occured while Reading Tsv");
        }
        return rowMaps;
    }

    public ArrayList<HashMap<String, String>> processTsv(byte[] fileContent, String fileName, String[] headers) throws ApiException {
        ArrayList<HashMap<String, String>> rowMaps = new ArrayList<>();
        try {
            if (fileName.split("\\.").length!=2||!fileName.split("\\.")[1].equals("tsv")) {
                throw new ApiException("File is not of .tsv type");
            }
            InputStream inputStream = new ByteArrayInputStream(fileContent);
            BufferedReader TSVReader = new BufferedReader(new InputStreamReader(inputStream));
            String[] headersActual = TSVReader.readLine().split("\t");
            verifyHeader(headersActual, headers);
            String row = null;
            int fileRowCount = 0;
            while ((row = TSVReader.readLine()) != null) {
                HashMap<String, String> rowMap = new HashMap<>();
                String[] rowContent = row.split("\t");
                if (rowContent.length == headers.length) {
                    for (int i = 0; i < rowContent.length; i++) {
                        // Normalize data: trim and convert to lowercase
                        String normalizedValue = rowContent[i] != null ? rowContent[i].trim().toLowerCase() : "";
                        rowMap.put(headers[i], normalizedValue);
                    }
                }
                rowMaps.add(rowMap);
                fileRowCount = fileRowCount + 1;
                if (fileRowCount > 500001) {
                    throw new ApiException("File Row count is greater than 500000");
                }
            }
        } catch (ApiException apiException) {
            throw apiException;
        } catch (Exception e) {
            throw new ApiException("Some Error occured while Reading Tsv");
        }
        return rowMaps;
    }

    private boolean verifyHeader(String[] headersActual, String[] headersExpected) throws ApiException {
        if (!Arrays.equals(headersActual, headersExpected)) {
            String headerString = Arrays.toString(headersActual);
            String actualHeaderString = Arrays.toString(headersExpected);
            throw new ApiException("Headers for File does Not Match Expected Headers Headers for File " + headerString + "Headers Expected" + actualHeaderString);
        }
        return true;
    }
}
