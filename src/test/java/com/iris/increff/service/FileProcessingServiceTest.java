package com.iris.increff.service;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.exception.ApiException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

public class FileProcessingServiceTest extends AbstractUnitTest {

    @Autowired
    private FileProcessingService fileProcessingService;

    @Test
    public void testProcessTsv_Success() throws ApiException {
        String[] testHeaders = {"name", "age", "city"};
        String tsvContent = "name\tage\tcity\nJohn\t25\tNew York\nJane\t30\tLos Angeles";
        MockMultipartFile file = new MockMultipartFile("file", "test.tsv", "text/tab-separated-values", tsvContent.getBytes());

        ArrayList<HashMap<String, String>> result = fileProcessingService.processTsv(file, testHeaders);

        assertEquals(2, result.size());
        
        // Verify data normalization (trimming and lowercase)
        HashMap<String, String> firstRow = result.get(0);
        assertEquals("john", firstRow.get("name"));
        assertEquals("25", firstRow.get("age"));
        assertEquals("new york", firstRow.get("city"));

        HashMap<String, String> secondRow = result.get(1);
        assertEquals("jane", secondRow.get("name"));
        assertEquals("30", secondRow.get("age"));
        assertEquals("los angeles", secondRow.get("city"));
    }

    @Test(expected = ApiException.class)
    public void testProcessTsv_InvalidFileType() throws ApiException {
        String[] testHeaders = {"name", "age"};
        String csvContent = "name,age\nJohn,25";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        fileProcessingService.processTsv(file, testHeaders);
    }

    @Test(expected = ApiException.class)
    public void testProcessTsv_HeaderMismatch() throws ApiException {
        String[] testHeaders = {"name", "age", "city"};
        String tsvContent = "name\tage\nJohn\t25"; // Missing city column
        MockMultipartFile file = new MockMultipartFile("file", "test.tsv", "text/tab-separated-values", tsvContent.getBytes());

        fileProcessingService.processTsv(file, testHeaders);
    }

    @Test
    public void testProcessTsv_MismatchedColumns() throws ApiException {
        String[] testHeaders = {"name", "age", "city"};
        String tsvContent = "name\tage\tcity\nJohn\t25\nJane\t30\tLA"; // First row missing city, second row complete
        MockMultipartFile file = new MockMultipartFile("file", "test.tsv", "text/tab-separated-values", tsvContent.getBytes());

        ArrayList<HashMap<String, String>> result = fileProcessingService.processTsv(file, testHeaders);

        assertEquals(2, result.size());
        
        // First row should have empty map due to column mismatch
        HashMap<String, String> firstRow = result.get(0);
        assertTrue(firstRow.isEmpty());

        // Second row should have all data
        HashMap<String, String> secondRow = result.get(1);
        assertEquals("jane", secondRow.get("name"));
        assertEquals("30", secondRow.get("age"));
        assertEquals("la", secondRow.get("city"));
    }

    @Test
    public void testProcessTsv_WithByteArray() throws ApiException {
        String[] testHeaders = {"product", "price"};
        String tsvContent = "product\tprice\nLaptop\t999.99\nMouse\t29.99";
        byte[] fileContent = tsvContent.getBytes();

        ArrayList<HashMap<String, String>> result = fileProcessingService.processTsv(fileContent, "test.tsv", testHeaders);

        assertEquals(2, result.size());
        
        HashMap<String, String> firstRow = result.get(0);
        assertEquals("laptop", firstRow.get("product"));
        assertEquals("999.99", firstRow.get("price"));

        HashMap<String, String> secondRow = result.get(1);
        assertEquals("mouse", secondRow.get("product"));
        assertEquals("29.99", secondRow.get("price"));
    }

    @Test(expected = ApiException.class)
    public void testProcessTsv_ExceedsRowLimit() throws ApiException {
        String[] testHeaders = {"id", "value"};
        StringBuilder largeTsvContent = new StringBuilder("id\tvalue\n");
        
        // Create content with more than 500,000 rows
        for (int i = 0; i < 500002; i++) {
            largeTsvContent.append(i).append("\tvalue").append(i).append("\n");
        }
        
        MockMultipartFile file = new MockMultipartFile("file", "large.tsv", "text/tab-separated-values", largeTsvContent.toString().getBytes());

        fileProcessingService.processTsv(file, testHeaders);
    }

    @Test
    public void testProcessTsv_DataNormalization() throws ApiException {
        String[] testHeaders = {"name", "category"};
        String tsvContent = "name\tcategory\n  UPPER CASE  \t  MiXeD cAsE  \n\tTRIMMED\t";
        MockMultipartFile file = new MockMultipartFile("file", "test.tsv", "text/tab-separated-values", tsvContent.getBytes());

        ArrayList<HashMap<String, String>> result = fileProcessingService.processTsv(file, testHeaders);

        assertEquals(2, result.size());
        
        HashMap<String, String> firstRow = result.get(0);
        assertEquals("upper case", firstRow.get("name"));
        assertEquals("mixed case", firstRow.get("category"));

        HashMap<String, String> secondRow = result.get(1);
        assertEquals("", secondRow.get("name")); // Empty after trimming
        assertEquals("trimmed", secondRow.get("category"));
    }
}