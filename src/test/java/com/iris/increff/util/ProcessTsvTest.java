package com.iris.increff.util;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.model.NoosResult;
import com.iris.increff.model.Style;
import com.iris.increff.model.Store;
import com.iris.increff.model.SKU;
import com.iris.increff.model.Sales;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class ProcessTsvTest extends AbstractUnitTest {

    // =================================================================
    // Test createNoosResultsTsv
    // =================================================================

    @Test
    public void testCreateNoosResultsTsv() throws IOException {
        List<NoosResult> results = new ArrayList<>();
        Date now = new Date();
        NoosResult r1 = new NoosResult();
        r1.setCategory("SHIRTS");
        r1.setStyleCode("STYLE001");
        r1.setStyleROS(new BigDecimal("1.25"));
        r1.setType("core");
        r1.setStyleRevContribution(new BigDecimal("0.15"));
        r1.setTotalQuantitySold(100);
        r1.setTotalRevenue(new BigDecimal("5000.00"));
        r1.setDaysAvailable(90);
        r1.setDaysWithSales(85);
        r1.setAvgDiscount(new BigDecimal("0.10"));
        r1.setCalculatedDate(now);
        results.add(r1);

        MockHttpServletResponse response = new MockHttpServletResponse();
        ProcessTsv.createNoosResultsTsv(results, response);

        assertEquals("text/tab-separated-values", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition").startsWith("attachment; filename=noos_results_"));

        String tsvOutput = response.getContentAsString();
        String[] lines = tsvOutput.split("\n");
        assertEquals(2, lines.length); // Header + 1 data row
        assertTrue(lines[0].startsWith("Category\tStyle Code"));
        assertTrue(lines[1].contains("SHIRTS\tSTYLE001\t1.25"));
        assertTrue(lines[1].contains(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now)));
    }

    // =================================================================
    // Test generateDataForTemplate
    // =================================================================

    @Test
    public void testGenerateDataForTemplate() {
        String[] headers = {"Header1", "Header2", "Header3"};
        String filename = "test_template.tsv";

        ResponseEntity<byte[]> response = ProcessTsv.generateDataForTemplate(headers, filename);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        String content = new String(response.getBody());
        assertTrue(content.contains("Header1\tHeader2\tHeader3"));
        assertTrue(content.contains("Value of Header1\tValue of Header2\tValue of Header3"));

        assertEquals("attachment; filename=" + filename,
                response.getHeaders().getFirst("Content-disposition"));
    }

    @Test
    public void testGenerateDataForTemplateWithSingleHeader() {
        String[] headers = {"SingleHeader"};
        String filename = "single.tsv";

        ResponseEntity<byte[]> response = ProcessTsv.generateDataForTemplate(headers, filename);

        assertNotNull(response);
        String content = new String(response.getBody());
        assertTrue(content.contains("SingleHeader"));
        assertTrue(content.contains("Value of SingleHeader"));
        assertFalse(content.contains("\t")); // No tabs for single column
    }

    @Test
    public void testGenerateDataForTemplateWithManyHeaders() {
        String[] headers = {"H1", "H2", "H3", "H4", "H5"};
        String filename = "multi.tsv";

        ResponseEntity<byte[]> response = ProcessTsv.generateDataForTemplate(headers, filename);

        assertNotNull(response);
        String content = new String(response.getBody());

        for (String header : headers) {
            assertTrue(content.contains(header));
            assertTrue(content.contains("Value of " + header));
        }
    }

    // =================================================================
    // Test createFileResponse with errors
    // =================================================================

    @Test
    public void testCreateFileResponseWithErrors() throws IOException {
        List<String> errors = Arrays.asList(
                "Error 1: Invalid data",
                "Error 2: Missing field",
                "Error 3: Validation failed"
        );
        String filename = "errors.csv";
        MockHttpServletResponse response = new MockHttpServletResponse();

        ProcessTsv.createFileResponse(errors, filename, response);

        assertEquals("application/csv", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition").contains(filename));

        String content = response.getContentAsString();
        assertTrue(content.contains("Errors in TSV"));
        assertTrue(content.contains("Error 1: Invalid data"));
        assertTrue(content.contains("Error 2: Missing field"));
        assertTrue(content.contains("Error 3: Validation failed"));
    }

    @Test
    public void testCreateFileResponseWithNoErrors() throws IOException {
        List<String> errors = null;
        String filename = "errors.csv";
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Should not throw exception
        ProcessTsv.createFileResponse(errors, filename, response);

        // Response should be empty
        assertEquals("", response.getContentAsString());
    }

    @Test
    public void testCreateFileResponseWithEmptyErrorList() throws IOException {
        List<String> errors = new ArrayList<>();
        String filename = "errors.csv";
        MockHttpServletResponse response = new MockHttpServletResponse();

        ProcessTsv.createFileResponse(errors, filename, response);

        String content = response.getContentAsString();
        assertTrue(content.contains("Errors in TSV"));
    }

    // =================================================================
    // Test createFileResponse with File
    // =================================================================

    @Test
    public void testCreateFileResponseWithFile() throws IOException {
        // Create a temporary file
        File tempFile = File.createTempFile("test", ".tsv");
        tempFile.deleteOnExit();

        MockHttpServletResponse response = new MockHttpServletResponse();

        ProcessTsv.createFileResponse(tempFile, response);

        assertEquals("application/octet-stream", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition").contains(tempFile.getName()));
    }

    // =================================================================
    // Test createStylesDataResponse
    // =================================================================

    @Test
    public void testCreateStylesDataResponse() throws IOException {
        List<Style> styles = new ArrayList<>();

        Style s1 = new Style();
        s1.setStyleCode("STYLE001");
        s1.setBrand("Nike");
        s1.setCategory("Shoes");
        s1.setSubCategory("Running");
        s1.setMrp(new BigDecimal("1999.99"));
        s1.setGender("Male");
        styles.add(s1);

        Style s2 = new Style();
        s2.setStyleCode("STYLE002");
        s2.setBrand("Adidas");
        s2.setCategory("Apparel");
        s2.setSubCategory("Tshirt");
        s2.setMrp(new BigDecimal("799.50"));
        s2.setGender("Female");
        styles.add(s2);

        String[] headers = {"Style Code", "Brand", "Category", "Sub Category", "MRP", "Gender"};
        MockHttpServletResponse response = new MockHttpServletResponse();

        ProcessTsv.createStylesDataResponse(styles, response, headers);

        assertEquals("text/tab-separated-values", response.getContentType());
        String contentDisposition = response.getHeader("Content-Disposition");
        assertNotNull("Content-Disposition header should not be null", contentDisposition);
        assertTrue("Content-Disposition should contain styles_data.tsv", contentDisposition.contains("styles_data.tsv"));

        String content = response.getContentAsString();
        String[] lines = content.split("\n");
        assertTrue("Should have at least header + data rows", lines.length >= 2); // Header + at least 1 data row

        assertTrue("Should contain STYLE001", content.contains("STYLE001"));
        assertTrue("Should contain Nike", content.contains("Nike"));
        assertTrue("Should contain STYLE002", content.contains("STYLE002"));
        assertTrue("Should contain Adidas", content.contains("Adidas"));
    }

    // =================================================================
    // Test createStoresDataResponse
    // =================================================================

    @Test
    public void testCreateStoresDataResponse() throws IOException {
        List<Store> stores = new ArrayList<>();

        Store st1 = new Store();
        st1.setBranch("Downtown Branch");
        st1.setCity("New York");
        stores.add(st1);

        Store st2 = new Store();
        st2.setBranch("Mall Branch");
        st2.setCity("Los Angeles");
        stores.add(st2);

        String[] headers = {"Branch", "City"};
        MockHttpServletResponse response = new MockHttpServletResponse();

        ProcessTsv.createStoresDataResponse(stores, response, headers);

        assertEquals("text/tab-separated-values", response.getContentType());

        String content = response.getContentAsString();
        assertTrue(content.contains("Downtown Branch"));
        assertTrue(content.contains("New York"));
        assertTrue(content.contains("Mall Branch"));
        assertTrue(content.contains("Los Angeles"));
    }

    // =================================================================
    // Test createSkusDataResponse
    // =================================================================

    @Test
    public void testCreateSkusDataResponse() throws IOException {
        List<SKU> skus = new ArrayList<>();

        Style style = new Style();
        style.setStyleCode("STYLE001");

        SKU sku1 = new SKU();
        sku1.setSku("SKU001");
        sku1.setSize("L");
        sku1.setStyle(style);
        skus.add(sku1);

        SKU sku2 = new SKU();
        sku2.setSku("SKU002");
        sku2.setSize("XL");
        sku2.setStyle(style);
        skus.add(sku2);

        String[] headers = {"SKU", "Style Code", "Size"};
        MockHttpServletResponse response = new MockHttpServletResponse();

        ProcessTsv.createSkusDataResponse(skus, response, headers);

        assertEquals("text/tab-separated-values", response.getContentType());

        String content = response.getContentAsString();
        assertTrue(content.contains("SKU001"));
        assertTrue(content.contains("SKU002"));
        assertTrue(content.contains("STYLE001"));
        assertTrue(content.contains("L"));
        assertTrue(content.contains("XL"));
    }

    // =================================================================
    // Test createSalesDataResponse
    // =================================================================

    @Test
    public void testCreateSalesDataResponse() throws IOException {
        List<Sales> salesList = new ArrayList<>();

        Style style = new Style();
        style.setStyleCode("STYLE001");

        SKU sku = new SKU();
        sku.setSku("SKU001");
        sku.setStyle(style);

        Store store = new Store();
        store.setBranch("Branch1");

        Sales s1 = new Sales();
        s1.setDate(new Date());
        s1.setSku(sku);
        s1.setStore(store);
        s1.setQuantity(10);
        s1.setDiscount(new BigDecimal("100.00"));
        s1.setRevenue(new BigDecimal("5000.00"));
        salesList.add(s1);

        String[] headers = {"Date", "SKU", "Store", "Quantity", "Discount", "Revenue"};
        MockHttpServletResponse response = new MockHttpServletResponse();

        ProcessTsv.createSalesDataResponse(salesList, response, headers);

        assertEquals("text/tab-separated-values", response.getContentType());

        String content = response.getContentAsString();
        assertTrue(content.contains("SKU001"));
        assertTrue(content.contains("Branch1"));
        assertTrue(content.contains("10"));
        assertTrue(content.contains("5000.00"));
    }

    // =================================================================
    // Test edge cases
    // =================================================================

    @Test
    public void testCreateNoosResultsTsvWithEmptyList() throws IOException {
        List<NoosResult> results = new ArrayList<>();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ProcessTsv.createNoosResultsTsv(results, response);

        String content = response.getContentAsString();
        String[] lines = content.split("\n");
        assertEquals(1, lines.length); // Only header
    }

    @Test
    public void testCreateStylesDataResponseWithEmptyList() throws IOException {
        List<Style> styles = new ArrayList<>();
        String[] headers = {"Style Code", "Brand", "Category", "Sub Category", "MRP", "Gender"};
        MockHttpServletResponse response = new MockHttpServletResponse();

        ProcessTsv.createStylesDataResponse(styles, response, headers);

        String content = response.getContentAsString();
        String[] lines = content.split("\n");
        assertEquals(1, lines.length); // Only header
    }
}
