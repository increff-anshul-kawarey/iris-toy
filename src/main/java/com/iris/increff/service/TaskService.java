package com.iris.increff.service;

import com.iris.increff.dao.TaskDao;
import com.iris.increff.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Task entities with proper transaction boundaries.
 * 
 * This service ensures tasks are persisted in separate transactions
 * so they are immediately visible to async workers and polling endpoints.
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-10-02
 */
@Service
public class TaskService {

    @Autowired
    private TaskDao taskDao;

    /**
     * Create and persist a task in a NEW transaction that commits immediately.
     * 
     * This is critical for async operations - the task must be committed
     * BEFORE the async thread starts, so it can read the task from the database.
     * 
     * Uses REQUIRES_NEW propagation to ensure a separate transaction that
     * commits independently of the caller's transaction.
     * 
     * @param task Task to persist
     * @return The persisted task with generated ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Task createTaskInNewTransaction(Task task) {
        taskDao.insert(task);
        return task; // Transaction commits when method returns
    }

    /**
     * Update a task in the current transaction
     * 
     * @param task Task to update
     */
    @Transactional
    public void updateTask(Task task) {
        taskDao.update(task);
    }

    /**
     * Get a task by ID
     * 
     * @param taskId Task ID
     * @return Task or null if not found
     */
    @Transactional(readOnly = true)
    public Task getTask(Long taskId) {
        return taskDao.select(taskId);
    }
}

