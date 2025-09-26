package com.iris.increff.util;

import com.iris.increff.model.NoosResult;
import com.iris.increff.model.Sales;
import com.iris.increff.model.SKU;
import com.iris.increff.model.Store;
import com.iris.increff.model.Style;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
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

    // Data export methods for downloading actual database data
    public static void createStylesDataResponse(List<Style> styles, HttpServletResponse response) throws IOException {
        StringBuilder csvData = new StringBuilder();
        csvData.append(String.join("\t", stylesHeaders)).append("\n");

        for (Style style : styles) {
            csvData.append(style.getStyleCode()).append("\t")
                   .append(style.getBrand()).append("\t")
                   .append(style.getCategory()).append("\t")
                   .append(style.getSubCategory()).append("\t")
                   .append(style.getMrp().toString()).append("\t")
                   .append(style.getGender()).append("\n");
        }

        createCsvResponse(csvData.toString(), "styles_data.tsv", response);
    }

    public static void createStoresDataResponse(List<Store> stores, HttpServletResponse response) throws IOException {
        StringBuilder csvData = new StringBuilder();
        csvData.append(String.join("\t", storeHeaders)).append("\n");

        for (Store store : stores) {
            csvData.append(store.getBranch()).append("\t")
                   .append(store.getCity()).append("\n");
        }

        createCsvResponse(csvData.toString(), "stores_data.tsv", response);
    }

    public static void createSkusDataResponse(List<SKU> skus, HttpServletResponse response) throws IOException {
        StringBuilder csvData = new StringBuilder();
        csvData.append(String.join("\t", skuHeaders)).append("\n");

        for (SKU sku : skus) {
            String styleCode = sku.getStyle() != null ? sku.getStyle().getStyleCode() : "";
            csvData.append(sku.getSku()).append("\t")
                   .append(styleCode).append("\t")
                   .append(sku.getSize()).append("\n");
        }

        createCsvResponse(csvData.toString(), "skus_data.tsv", response);
    }

    public static void createSalesDataResponse(List<Sales> sales, HttpServletResponse response) throws IOException {
        StringBuilder csvData = new StringBuilder();
        csvData.append(String.join("\t", salesHeaders)).append("\n");

        for (Sales sale : sales) {
            String skuCode = sale.getSku() != null ? sale.getSku().getSku() : "";
            String channel = sale.getStore() != null ? sale.getStore().getBranch() : "";
            String day = sale.getDate() != null ? sale.getDate().toString() : "";

            csvData.append(day).append("\t")
                   .append(skuCode).append("\t")
                   .append(channel).append("\t")
                   .append(sale.getQuantity().toString()).append("\t")
                   .append(sale.getDiscount().toString()).append("\t")
                   .append(sale.getRevenue().toString()).append("\n");
        }

        createCsvResponse(csvData.toString(), "sales_data.tsv", response);
    }

    /**
     * Create NOOS Results TSV for download
     * 
     * PRD Requirement: "TSV file download for algorithm results"
     * Output Format: Category | Style Code | Style ROS | Type | Style Rev Contri
     * 
     * @param results List of NOOS results
     * @param response HTTP response for file download
     * @throws IOException if file creation fails
     */
    public static void createNoosResultsTsv(List<NoosResult> results, HttpServletResponse response) throws IOException {
        StringBuilder csvData = new StringBuilder();
        
        // Add header (PRD specified format)
        csvData.append("Category\tStyle Code\tStyle ROS\tType\tStyle Rev Contri\tTotal Quantity\tTotal Revenue\tDays Available\tDays With Sales\tAvg Discount\tCalculated Date\n");
        
        // Add data rows
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (NoosResult result : results) {
            csvData.append(result.getCategory()).append("\t")
                   .append(result.getStyleCode()).append("\t")
                   .append(result.getStyleROS()).append("\t")
                   .append(result.getType()).append("\t")
                   .append(result.getStyleRevContribution()).append("\t")
                   .append(result.getTotalQuantitySold()).append("\t")
                   .append(result.getTotalRevenue()).append("\t")
                   .append(result.getDaysAvailable()).append("\t")
                   .append(result.getDaysWithSales()).append("\t")
                   .append(result.getAvgDiscount()).append("\t")
                   .append(dateFormat.format(result.getCalculatedDate())).append("\n");
        }
        
        // Generate filename with timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String fileName = "noos_results_" + timestamp + ".tsv";
        
        createCsvResponse(csvData.toString(), fileName, response);
    }

    private static void createCsvResponse(String csvData, String fileName, HttpServletResponse response) throws IOException {
        response.setContentType("text/tab-separated-values");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.setCharacterEncoding("UTF-8");

        OutputStream os = response.getOutputStream();
        os.write(csvData.getBytes("UTF-8"));
        os.flush();
        closeQuietly(os);
    }

}