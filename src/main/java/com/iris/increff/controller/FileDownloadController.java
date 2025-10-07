package com.iris.increff.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller for handling file downloads, particularly error files from upload operations
 * 
 * Provides endpoints to download:
 * - Failed rows files (original data)
 * - Failed rows with error details files
 * - Error summary reports
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/api/download")
public class FileDownloadController {

    /**
     * Download error files generated during upload processing
     * 
     * @param filePath Absolute path to the file to download
     * @return ResponseEntity with file content or error response
     */
    @GetMapping("/error-file")
    public ResponseEntity<Resource> downloadErrorFile(@RequestParam String filePath) {
        try {
            // Security check: ensure file is in temp directory and has expected naming pattern
            Path path = Paths.get(filePath);
            String fileName = path.getFileName().toString();
            
            // Validate file is in temp directory
            String tempDir = System.getProperty("java.io.tmpdir");
            if (!filePath.startsWith(tempDir)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
            }
            
            // Validate file name pattern (security measure)
            if (!fileName.matches("^(failed_|error_summary_).*\\.tsv$")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
            }
            
            File file = new File(filePath);
            if (!file.exists() || !file.canRead()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            // Determine content type
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }
    
    /**
     * Download error file by type and task ID (alternative endpoint)
     * 
     * @param taskId Task ID that generated the error files
     * @param fileType Type of error file (failedRows, failedRowsWithErrors, errorSummary)
     * @return ResponseEntity with file content or error response
     */
    @GetMapping("/error-file/{taskId}/{fileType}")
    public ResponseEntity<Resource> downloadErrorFileByType(
            @PathVariable String taskId,
            @PathVariable String fileType) {
        
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            String fileName;
            
            // Map file type to expected file name pattern
            switch (fileType.toLowerCase()) {
                case "failedrows":
                    fileName = String.format("failed_*_rows_%s_*.tsv", taskId);
                    break;
                case "failedrowswitherrors":
                    fileName = String.format("failed_*_rows_with_errors_%s_*.tsv", taskId);
                    break;
                case "errorsummary":
                    fileName = String.format("error_summary_*_%s_*.tsv", taskId);
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }
            
            // Find matching file in temp directory
            File tempDirFile = new File(tempDir);
            File[] matchingFiles = tempDirFile.listFiles((dir, name) -> 
                name.matches(fileName.replace("*", ".*")));
            
            if (matchingFiles == null || matchingFiles.length == 0) {
                return ResponseEntity.notFound().build();
            }
            
            // Get the most recent file if multiple matches
            File file = matchingFiles[0];
            for (File f : matchingFiles) {
                if (f.lastModified() > file.lastModified()) {
                    file = f;
                }
            }
            
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/tab-separated-values"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }
    
    /**
     * Clean up old error files (maintenance endpoint)
     * 
     * @param olderThanHours Delete files older than specified hours (default: 24)
     * @return ResponseEntity with cleanup summary
     */
    @DeleteMapping("/cleanup-error-files")
    public ResponseEntity<String> cleanupErrorFiles(@RequestParam(defaultValue = "24") int olderThanHours) {
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            File tempDirFile = new File(tempDir);
            
            long cutoffTime = System.currentTimeMillis() - (olderThanHours * 60 * 60 * 1000L);
            int deletedCount = 0;
            
            File[] errorFiles = tempDirFile.listFiles((dir, name) -> 
                name.matches("^(failed_|error_summary_).*\\.tsv$"));
            
            if (errorFiles != null) {
                for (File file : errorFiles) {
                    if (file.lastModified() < cutoffTime) {
                        if (file.delete()) {
                            deletedCount++;
                        }
                    }
                }
            }
            
            return ResponseEntity.ok("Cleaned up " + deletedCount + " error files older than " + olderThanHours + " hours");
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error during cleanup: " + e.getMessage());
        }
    }
}
