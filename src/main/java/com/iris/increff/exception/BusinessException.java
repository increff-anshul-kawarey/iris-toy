package com.iris.increff.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom business exception class extending RuntimeException.
 * Used for handling business logic errors and validation failures.
 * Includes error codes and static factory methods for common scenarios.
 */
public class BusinessException extends RuntimeException {

    private static final Logger logger = LoggerFactory.getLogger(BusinessException.class);
    private static final long serialVersionUID = 1L;

    // Error codes for different business scenarios
    public static final String ENTITY_NOT_FOUND = "ENTITY_NOT_FOUND";
    public static final String INVALID_DATA = "INVALID_DATA";
    public static final String DUPLICATE_ENTRY = "DUPLICATE_ENTRY";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String BUSINESS_RULE_VIOLATION = "BUSINESS_RULE_VIOLATION";
    public static final String OPERATION_NOT_ALLOWED = "OPERATION_NOT_ALLOWED";

    private final String errorCode;

    /**
     * Constructs a new BusinessException with the specified detail message.
     * 
     * @param message the detail message
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = BUSINESS_RULE_VIOLATION;
        logger.error("BusinessException created: {}", message);
    }

    /**
     * Constructs a new BusinessException with the specified detail message and error code.
     * 
     * @param message the detail message
     * @param errorCode the error code
     */
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : BUSINESS_RULE_VIOLATION;
        logger.error("BusinessException created with code {}: {}", this.errorCode, message);
    }

    /**
     * Constructs a new BusinessException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = BUSINESS_RULE_VIOLATION;
        logger.error("BusinessException created with cause: {}", message, cause);
    }

    /**
     * Constructs a new BusinessException with the specified detail message, cause, and error code.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     * @param errorCode the error code
     */
    public BusinessException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : BUSINESS_RULE_VIOLATION;
        logger.error("BusinessException created with code {} and cause: {}", this.errorCode, message, cause);
    }

    /**
     * Gets the error code associated with this exception.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Creates a BusinessException for entity not found scenarios.
     * 
     * @param entityName the name of the entity that was not found
     * @param id the ID of the entity that was not found
     * @return a new BusinessException with appropriate message and error code
     */
    public static BusinessException entityNotFound(String entityName, Object id) {
        if (entityName == null || entityName.trim().isEmpty()) {
            entityName = "Entity";
        }
        
        String message = String.format("%s with ID '%s' not found", entityName, id);
        logger.debug("Creating entity not found exception: {}", message);
        return new BusinessException(message, ENTITY_NOT_FOUND);
    }

    /**
     * Creates a BusinessException for invalid data scenarios.
     * 
     * @param fieldName the name of the field with invalid data
     * @param value the invalid value
     * @return a new BusinessException with appropriate message and error code
     */
    public static BusinessException invalidData(String fieldName, Object value) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            fieldName = "Field";
        }
        
        String message = String.format("Invalid data for field '%s': '%s'", fieldName, value);
        logger.debug("Creating invalid data exception: {}", message);
        return new BusinessException(message, INVALID_DATA);
    }

    /**
     * Creates a BusinessException for duplicate entry scenarios.
     * 
     * @param fieldName the name of the field with duplicate value
     * @param value the duplicate value
     * @return a new BusinessException with appropriate message and error code
     */
    public static BusinessException duplicateEntry(String fieldName, Object value) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            fieldName = "Field";
        }
        
        String message = String.format("Duplicate entry for field '%s': '%s'", fieldName, value);
        logger.debug("Creating duplicate entry exception: {}", message);
        return new BusinessException(message, DUPLICATE_ENTRY);
    }

    /**
     * Creates a BusinessException for validation error scenarios.
     * 
     * @param message the validation error message
     * @return a new BusinessException with appropriate error code
     */
    public static BusinessException validationError(String message) {
        if (message == null || message.trim().isEmpty()) {
            message = "Validation error occurred";
        }
        
        logger.debug("Creating validation error exception: {}", message);
        return new BusinessException(message, VALIDATION_ERROR);
    }

    /**
     * Creates a BusinessException for business rule violation scenarios.
     * 
     * @param message the business rule violation message
     * @return a new BusinessException with appropriate error code
     */
    public static BusinessException businessRuleViolation(String message) {
        if (message == null || message.trim().isEmpty()) {
            message = "Business rule violation occurred";
        }
        
        logger.debug("Creating business rule violation exception: {}", message);
        return new BusinessException(message, BUSINESS_RULE_VIOLATION);
    }

    /**
     * Creates a BusinessException for operation not allowed scenarios.
     * 
     * @param operation the operation that is not allowed
     * @param reason the reason why the operation is not allowed
     * @return a new BusinessException with appropriate message and error code
     */
    public static BusinessException operationNotAllowed(String operation, String reason) {
        if (operation == null || operation.trim().isEmpty()) {
            operation = "Operation";
        }
        
        String message = reason != null && !reason.trim().isEmpty() 
            ? String.format("Operation '%s' is not allowed: %s", operation, reason)
            : String.format("Operation '%s' is not allowed", operation);
        
        logger.debug("Creating operation not allowed exception: {}", message);
        return new BusinessException(message, OPERATION_NOT_ALLOWED);
    }

    /**
     * Creates a BusinessException for entity not found scenarios with custom message.
     * 
     * @param entityName the name of the entity that was not found
     * @param fieldName the field name used for lookup
     * @param value the value used for lookup
     * @return a new BusinessException with appropriate message and error code
     */
    public static BusinessException entityNotFound(String entityName, String fieldName, Object value) {
        if (entityName == null || entityName.trim().isEmpty()) {
            entityName = "Entity";
        }
        if (fieldName == null || fieldName.trim().isEmpty()) {
            fieldName = "ID";
        }
        
        String message = String.format("%s with %s '%s' not found", entityName, fieldName, value);
        logger.debug("Creating entity not found exception: {}", message);
        return new BusinessException(message, ENTITY_NOT_FOUND);
    }

    /**
     * Creates a BusinessException for constraint violation scenarios.
     * 
     * @param constraintName the name of the constraint that was violated
     * @param details additional details about the violation
     * @return a new BusinessException with appropriate message and error code
     */
    public static BusinessException constraintViolation(String constraintName, String details) {
        if (constraintName == null || constraintName.trim().isEmpty()) {
            constraintName = "Constraint";
        }
        
        String message = details != null && !details.trim().isEmpty()
            ? String.format("Constraint violation: %s - %s", constraintName, details)
            : String.format("Constraint violation: %s", constraintName);
        
        logger.debug("Creating constraint violation exception: {}", message);
        return new BusinessException(message, BUSINESS_RULE_VIOLATION);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Checks if this exception has a specific error code.
     * 
     * @param errorCode the error code to check
     * @return true if this exception has the specified error code
     */
    public boolean hasErrorCode(String errorCode) {
        return this.errorCode.equals(errorCode);
    }

    /**
     * Checks if this exception represents an entity not found error.
     * 
     * @return true if this is an entity not found exception
     */
    public boolean isEntityNotFound() {
        return hasErrorCode(ENTITY_NOT_FOUND);
    }

    /**
     * Checks if this exception represents an invalid data error.
     * 
     * @return true if this is an invalid data exception
     */
    public boolean isInvalidData() {
        return hasErrorCode(INVALID_DATA);
    }

    /**
     * Checks if this exception represents a duplicate entry error.
     * 
     * @return true if this is a duplicate entry exception
     */
    public boolean isDuplicateEntry() {
        return hasErrorCode(DUPLICATE_ENTRY);
    }

    /**
     * Checks if this exception represents a validation error.
     * 
     * @return true if this is a validation error exception
     */
    public boolean isValidationError() {
        return hasErrorCode(VALIDATION_ERROR);
    }

    /**
     * Checks if this exception represents a business rule violation.
     * 
     * @return true if this is a business rule violation exception
     */
    public boolean isBusinessRuleViolation() {
        return hasErrorCode(BUSINESS_RULE_VIOLATION);
    }

    /**
     * Checks if this exception represents an operation not allowed error.
     * 
     * @return true if this is an operation not allowed exception
     */
    public boolean isOperationNotAllowed() {
        return hasErrorCode(OPERATION_NOT_ALLOWED);
    }

    @Override
    public String toString() {
        return String.format("BusinessException{errorCode='%s', message='%s'}", 
                           errorCode, getMessage());
    }
}
