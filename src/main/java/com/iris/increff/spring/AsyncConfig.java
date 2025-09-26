package com.iris.increff.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async Configuration for Background Processing
 * 
 * Enables asynchronous processing for:
 * - File uploads (styles, SKUs, sales, stores)
 * - Algorithm execution (NOOS)
 * - File exports/downloads
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Thread pool executor for NOOS operations
     * 
     * Configuration:
     * - Core pool size: 2 threads (always active)
     * - Max pool size: 4 threads (scale up under load)
     * - Queue capacity: 10 tasks (bounded to prevent memory issues)
     * - Keep alive: 60 seconds for idle threads
     * 
     * Back-pressure: When queue is full, reject new tasks with custom handler
     */
    @Bean(name = "noosExecutor")
    public Executor noosExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Thread pool configuration
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setKeepAliveSeconds(60);
        
        // Thread naming for easier debugging
        executor.setThreadNamePrefix("NOOS-");
        
        // Graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // Custom rejection handler for back-pressure
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                logger.warn("🚫 Task rejected - thread pool queue is full. " +
                           "Active: {}, Queue: {}, Pool: {}", 
                           executor.getActiveCount(), 
                           executor.getQueue().size(),
                           executor.getPoolSize());
                
                // This will cause the calling thread to handle the rejection
                // The controller should catch this and return HTTP 429
                throw new RuntimeException("Thread pool queue is full. Too many concurrent tasks.");
            }
        });
        
        executor.initialize();
        
        logger.info("🚀 NOOS Executor initialized: core={}, max={}, queue={}", 
                   executor.getCorePoolSize(), 
                   executor.getMaxPoolSize(), 
                   10); // Queue capacity is set above
        
        return executor;
    }

    /**
     * Separate executor for file operations (uploads/downloads)
     * 
     * File operations are I/O intensive and may take longer,
     * so we use a separate pool to avoid blocking algorithm execution
     */
    @Bean(name = "fileExecutor")
    public Executor fileExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Larger pool for I/O operations
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(15);
        executor.setKeepAliveSeconds(120);
        
        executor.setThreadNamePrefix("FILE-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // Same rejection handler
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                logger.warn("🚫 File task rejected - thread pool queue is full. " +
                           "Active: {}, Queue: {}, Pool: {}", 
                           executor.getActiveCount(), 
                           executor.getQueue().size(),
                           executor.getPoolSize());
                
                throw new RuntimeException("File processing queue is full. Too many concurrent uploads/downloads.");
            }
        });
        
        executor.initialize();
        
        logger.info("📁 File Executor initialized: core={}, max={}, queue={}", 
                   executor.getCorePoolSize(), 
                   executor.getMaxPoolSize(), 
                   15); // Queue capacity is set above
        
        return executor;
    }
}
