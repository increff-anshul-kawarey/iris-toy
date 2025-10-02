package com.iris.increff.service;

import com.iris.increff.dao.*;
import com.iris.increff.model.*;
import com.iris.increff.util.ProcessTsv;
import com.iris.increff.config.TsvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous Download Service for generating large TSV exports without blocking request threads.
 * 
 * PRD: File downloads must be asynchronous with Task tracking and progress updates.
 */
@Service
public class AsyncDownloadService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncDownloadService.class);

    @Autowired
    private StyleDao styleDao;

    @Autowired
    private StoreDao storeDao;

    @Autowired
    private SkuDao skuDao;

    @Autowired
    private SalesDao salesDao;

    @Autowired
    private NoosResultDao noosResultDao;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private TsvProperties tsvProperties;

    @Async("fileExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Task> downloadStylesAsync(Long taskId) {
        return generateSimpleTsv(taskId, "STYLES_DOWNLOAD", () -> {
            List<Style> styles = styleDao.findAll();
            StringBuilder csv = new StringBuilder();
            csv.append(String.join("\t", tsvProperties.getStylesHeaders())).append("\n");
            for (Style style : styles) {
                csv.append(style.getStyleCode()).append("\t")
                   .append(style.getBrand()).append("\t")
                   .append(style.getCategory()).append("\t")
                   .append(style.getSubCategory()).append("\t")
                   .append(style.getMrp().toString()).append("\t")
                   .append(style.getGender()).append("\n");
            }
            return new GeneratedFile("styles_data_" + nowTs() + ".tsv", csv.toString());
        });
    }

    @Async("fileExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Task> downloadStoresAsync(Long taskId) {
        return generateSimpleTsv(taskId, "STORES_DOWNLOAD", () -> {
            List<Store> stores = storeDao.findAll();
            StringBuilder csv = new StringBuilder();
            csv.append(String.join("\t", tsvProperties.getStoreHeaders())).append("\n");
            for (Store store : stores) {
                csv.append(store.getBranch()).append("\t")
                   .append(store.getCity()).append("\n");
            }
            return new GeneratedFile("stores_data_" + nowTs() + ".tsv", csv.toString());
        });
    }

    @Async("fileExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Task> downloadSkusAsync(Long taskId) {
        return generateSimpleTsv(taskId, "SKUS_DOWNLOAD", () -> {
            List<SKU> skus = skuDao.findAll();
            StringBuilder csv = new StringBuilder();
            csv.append(String.join("\t", tsvProperties.getSkuHeaders())).append("\n");
            for (SKU sku : skus) {
                String styleCode = sku.getStyle() != null ? sku.getStyle().getStyleCode() : "";
                csv.append(sku.getSku()).append("\t")
                   .append(styleCode).append("\t")
                   .append(sku.getSize()).append("\n");
            }
            return new GeneratedFile("skus_data_" + nowTs() + ".tsv", csv.toString());
        });
    }

    @Async("fileExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Task> downloadSalesAsync(Long taskId) {
        return generateSimpleTsv(taskId, "SALES_DOWNLOAD", () -> {
            List<Sales> sales = salesDao.findAll();
            StringBuilder csv = new StringBuilder();
            csv.append(String.join("\t", tsvProperties.getSalesHeaders())).append("\n");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            for (Sales sale : sales) {
                String skuCode = sale.getSku() != null ? sale.getSku().getSku() : "";
                String channel = sale.getStore() != null ? sale.getStore().getBranch() : "";
                String day = sale.getDate() != null ? df.format(sale.getDate()) : "";
                csv.append(day).append("\t")
                   .append(skuCode).append("\t")
                   .append(channel).append("\t")
                   .append(sale.getQuantity().toString()).append("\t")
                   .append(sale.getDiscount().toString()).append("\t")
                   .append(sale.getRevenue().toString()).append("\n");
            }
            return new GeneratedFile("sales_data_" + nowTs() + ".tsv", csv.toString());
        });
    }

    @Async("fileExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Task> downloadNoosResultsAsync(Long taskId, Long runId) {
        return generateSimpleTsv(taskId, "NOOS_DOWNLOAD", () -> {
            List<NoosResult> results = (runId != null) ?
                    noosResultDao.getResultsByRunId(runId) :
                    noosResultDao.getLatestResults();
            StringBuilder csv = new StringBuilder();
            // Header exactly as in ProcessTsv.createNoosResultsTsv
            csv.append("Category\tStyle Code\tStyle ROS\tType\tStyle Rev Contri\tTotal Quantity\tTotal Revenue\tDays Available\tDays With Sales\tAvg Discount\tCalculated Date\n");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (NoosResult result : results) {
                csv.append(result.getCategory()).append("\t")
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
            return new GeneratedFile("noos_results_" + nowTs() + ".tsv", csv.toString());
        });
    }

    private CompletableFuture<Task> generateSimpleTsv(Long taskId, String type, FileBuilder builder) {
        Task task = taskDao.select(taskId);
        if (task == null) {
            // Retry briefly to allow outer transaction to commit the task
            for (int i = 0; i < 10 && task == null; i++) {
                try { Thread.sleep(50); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                task = taskDao.select(taskId);
            }
            if (task == null) {
                logger.error("❌ Task not found after retries: {}", taskId);
                return CompletableFuture.completedFuture(null);
            }
        }

        try {
            task.setStatus("RUNNING");
            task.updateProgress(5.0, "INITIALIZING", "Preparing download...");
            taskDao.update(task);

            if (checkCancellation(task)) {
                return CompletableFuture.completedFuture(task);
            }

            task.updateProgress(20.0, "GATHERING", "Fetching records from database...");
            taskDao.update(task);

            // Removed artificial test delay for better performance

            GeneratedFile generated = builder.build();
            String absPath = writeToTemp(generated.fileName, generated.content);

            task.updateProgress(90.0, "FINALIZING", "Writing TSV to file...");
            task.setResultType("TSV");
            task.setResultUrl(absPath); // absolute file system path
            task.setProcessedRecords(countLines(generated.content));
            task.setTotalRecords(task.getProcessedRecords());
            taskDao.update(task);

            task.setStatus("COMPLETED");
            task.setEndTime(new Date());
            task.updateProgress(100.0, "COMPLETED", type + " completed");
            taskDao.update(task);
            logger.info("✅ {} ready at {}", type, absPath);

        } catch (Exception e) {
            logger.error("❌ {} failed for task {}: {}", type, taskId, e.getMessage(), e);
            failTask(task, type + " failed: " + e.getMessage());
        }

        return CompletableFuture.completedFuture(task);
    }


    private boolean checkCancellation(Task task) {
        Task refreshed = taskDao.select(task.getId());
        if (refreshed != null && refreshed.isCancellationRequested()) {
            logger.info("🛑 Cancellation detected for task: {}", task.getId());
            task.setStatus("CANCELLED");
            task.updateProgress(task.getProgressPercentage(), "CANCELLED", "Download was cancelled by user");
            taskDao.update(task);
            return true;
        }
        return false;
    }

    private void failTask(Task task, String errorMessage) {
        task.setStatus("FAILED");
        task.setEndTime(new Date());
        task.setErrorMessage(errorMessage);
        task.updateProgress(task.getProgressPercentage(), "FAILED", errorMessage);
        taskDao.update(task);
    }

    private String writeToTemp(String fileName, String content) throws Exception {
        File dir = new File(System.getProperty("java.io.tmpdir"), "iris-downloads");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File out = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        }
        return out.getAbsolutePath();
    }

    private int countLines(String content) {
        if (content == null || content.isEmpty()) return 0;
        int lines = 0;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                lines++;
            }
        }
        // subtract header line
        return Math.max(0, lines - 1);
    }

    private String nowTs() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    private interface FileBuilder {
        GeneratedFile build() throws Exception;
    }

    private static class GeneratedFile {
        final String fileName;
        final String content;
        GeneratedFile(String fileName, String content) {
            this.fileName = fileName;
            this.content = content;
        }
    }
}


