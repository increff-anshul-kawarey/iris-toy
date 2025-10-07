package com.iris.increff.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * Configurable Validation Service for comprehensive input validation
 * 
 * Provides flexible validation with configurable rules instead of hardcoded values.
 * Supports validation for all entity types with customizable constraints.
 * 
 * @author Anshuk Kawarry
 * @version 3.0
 * @since 2025-01-01
 */
@Service
public class ValidationService {

    // Configurable properties with sensible defaults
    @Value("${validation.date.format:yyyy-MM-dd}")
    private String dateFormat;
    
    @Value("${validation.string.min-length:1}")
    private int minStringLength;
    
    @Value("${validation.string.max-length:255}")
    private int maxStringLength;
    
    @Value("${validation.code.min-length:3}")
    private int minCodeLength;
    
    @Value("${validation.code.max-length:50}")
    private int maxCodeLength;
    
    @Value("${validation.price.min:0.01}")
    private BigDecimal minPrice;
    
    @Value("${validation.price.max:1000000}")
    private BigDecimal maxPrice;
    
    @Value("${validation.quantity.min:1}")
    private int minQuantity;
    
    @Value("${validation.quantity.max:999999}")
    private int maxQuantity;
    
    @Value("${validation.decimal.max-scale:2}")
    private int maxDecimalScale;

    // Flexible regex patterns
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[A-Za-z0-9\\s&.-]+$");
    private static final Pattern ALPHABETIC_PATTERN = Pattern.compile("^[A-Za-z\\s]+$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");

    /**
     * Validation result containing the validation status and error message
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
    }

    // ==================== GENERIC VALIDATION METHODS ====================
    
    /**
     * Validate that a string is not null or empty
     */
    public ValidationResult validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.error(fieldName + " cannot be empty");
        }
        return ValidationResult.success();
    }
    
    /**
     * Validate string length constraints
     */
    public ValidationResult validateStringLength(String value, String fieldName, int minLength, int maxLength) {
        if (value == null) {
            return ValidationResult.error(fieldName + " cannot be null");
        }
        
        String trimmed = value.trim();
        if (trimmed.length() < minLength || trimmed.length() > maxLength) {
            return ValidationResult.error(fieldName + " must be between " + minLength + 
                                        " and " + maxLength + " characters, found: " + trimmed.length());
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validate string against a pattern
     */
    public ValidationResult validatePattern(String value, String fieldName, Pattern pattern, String patternDescription) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.error(fieldName + " cannot be empty");
        }
        
        if (!pattern.matcher(value.trim()).matches()) {
            return ValidationResult.error(fieldName + " format is invalid. " + patternDescription);
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validate numeric string and parse to integer
     */
    public ValidationResult validateInteger(String value, String fieldName, int minValue, int maxValue) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.error(fieldName + " cannot be empty");
        }
        
        try {
            int intValue = Integer.parseInt(value.trim());
            if (intValue < minValue || intValue > maxValue) {
                return ValidationResult.error(fieldName + " must be between " + minValue + 
                                            " and " + maxValue + ", found: " + intValue);
            }
            return ValidationResult.success();
        } catch (NumberFormatException e) {
            return ValidationResult.error(fieldName + " must be a valid integer, found: " + value.trim());
        }
    }
    
    /**
     * Validate decimal string and parse to BigDecimal
     */
    public ValidationResult validateDecimal(String value, String fieldName, BigDecimal minValue, BigDecimal maxValue) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.error(fieldName + " cannot be empty");
        }
        
        try {
            BigDecimal decimalValue = new BigDecimal(value.trim());
            
            if (decimalValue.compareTo(minValue) < 0 || decimalValue.compareTo(maxValue) > 0) {
                return ValidationResult.error(fieldName + " must be between " + minValue + 
                                            " and " + maxValue + ", found: " + decimalValue);
            }
            
            if (decimalValue.scale() > maxDecimalScale) {
                return ValidationResult.error(fieldName + " cannot have more than " + maxDecimalScale + 
                                            " decimal places, found: " + decimalValue.scale());
            }
            
            return ValidationResult.success();
        } catch (NumberFormatException e) {
            return ValidationResult.error(fieldName + " must be a valid decimal number, found: " + value.trim());
        }
    }
    
    /**
     * Validate date string
     */
    public ValidationResult validateDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.error("Date cannot be empty");
        }
        
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            formatter.setLenient(false);
            formatter.parse(value.trim());
            return ValidationResult.success();
        } catch (ParseException e) {
            return ValidationResult.error("Invalid date format. Expected format: " + dateFormat + ", found: " + value.trim());
        }
    }

    // ==================== SPECIFIC FIELD VALIDATIONS ====================
    
    /**
     * Validate style code - flexible format
     */
    public ValidationResult validateStyleCode(String styleCode) {
        ValidationResult emptyCheck = validateNotEmpty(styleCode, "Style code");
        if (!emptyCheck.isValid()) return emptyCheck;
        
        ValidationResult lengthCheck = validateStringLength(styleCode, "Style code", minCodeLength, maxCodeLength);
        if (!lengthCheck.isValid()) return lengthCheck;
        
        // Allow any alphanumeric code format
        return validatePattern(styleCode.trim().toUpperCase(), "Style code", CODE_PATTERN, 
                             "Style code can only contain letters and numbers");
    }
    
    /**
     * Validate brand name
     */
    public ValidationResult validateBrand(String brand) {
        ValidationResult emptyCheck = validateNotEmpty(brand, "Brand");
        if (!emptyCheck.isValid()) return emptyCheck;
        
        ValidationResult lengthCheck = validateStringLength(brand, "Brand", minStringLength, maxStringLength);
        if (!lengthCheck.isValid()) return lengthCheck;
        
        return validatePattern(brand.trim(), "Brand", ALPHANUMERIC_PATTERN, 
                             "Brand can only contain letters, numbers, spaces, &, ., and -");
    }
    
    /**
     * Validate category - flexible validation
     */
    public ValidationResult validateCategory(String category) {
        ValidationResult emptyCheck = validateNotEmpty(category, "Category");
        if (!emptyCheck.isValid()) return emptyCheck;
        
        ValidationResult lengthCheck = validateStringLength(category, "Category", minStringLength, maxStringLength);
        if (!lengthCheck.isValid()) return lengthCheck;
        
        return validatePattern(category.trim(), "Category", ALPHANUMERIC_PATTERN, 
                             "Category can only contain letters, numbers, spaces, &, ., and -");
    }
    
    /**
     * Validate sub-category - flexible validation
     */
    public ValidationResult validateSubCategory(String subCategory) {
        ValidationResult emptyCheck = validateNotEmpty(subCategory, "Sub-category");
        if (!emptyCheck.isValid()) return emptyCheck;
        
        ValidationResult lengthCheck = validateStringLength(subCategory, "Sub-category", minStringLength, maxStringLength);
        if (!lengthCheck.isValid()) return lengthCheck;
        
        return validatePattern(subCategory.trim(), "Sub-category", ALPHANUMERIC_PATTERN, 
                             "Sub-category can only contain letters, numbers, spaces, &, ., and -");
    }
    
    /**
     * Validate MRP (Maximum Retail Price)
     */
    public ValidationResult validateMrp(String mrpStr) {
        return validateDecimal(mrpStr, "MRP", minPrice, maxPrice);
    }
    
    /**
     * Validate gender - flexible validation
     */
    public ValidationResult validateGender(String gender) {
        ValidationResult emptyCheck = validateNotEmpty(gender, "Gender");
        if (!emptyCheck.isValid()) return emptyCheck;
        
        ValidationResult lengthCheck = validateStringLength(gender, "Gender", minStringLength, 20);
        if (!lengthCheck.isValid()) return lengthCheck;
        
        return validatePattern(gender.trim(), "Gender", ALPHABETIC_PATTERN, 
                             "Gender can only contain letters and spaces");
    }
    
    /**
     * Validate SKU code - flexible format
     */
    public ValidationResult validateSkuCode(String skuCode) {
        ValidationResult emptyCheck = validateNotEmpty(skuCode, "SKU code");
        if (!emptyCheck.isValid()) return emptyCheck;
        
        ValidationResult lengthCheck = validateStringLength(skuCode, "SKU code", minCodeLength, maxCodeLength);
        if (!lengthCheck.isValid()) return lengthCheck;
        
        return validatePattern(skuCode.trim().toUpperCase(), "SKU code", CODE_PATTERN, 
                             "SKU code can only contain letters and numbers");
    }
    
    /**
     * Validate size - flexible validation
     */
    public ValidationResult validateSize(String size) {
        ValidationResult emptyCheck = validateNotEmpty(size, "Size");
        if (!emptyCheck.isValid()) return emptyCheck;
        
        ValidationResult lengthCheck = validateStringLength(size, "Size", minStringLength, 20);
        if (!lengthCheck.isValid()) return lengthCheck;
        
        return validatePattern(size.trim(), "Size", ALPHANUMERIC_PATTERN, 
                             "Size can only contain letters, numbers, and spaces");
    }
    
    /**
     * Validate branch - flexible format
     */
    public ValidationResult validateBranch(String branch) {
        ValidationResult emptyCheck = validateNotEmpty(branch, "Branch");
        if (!emptyCheck.isValid()) return emptyCheck;
        
        ValidationResult lengthCheck = validateStringLength(branch, "Branch", minCodeLength, maxCodeLength);
        if (!lengthCheck.isValid()) return lengthCheck;
        
        return validatePattern(branch.trim().toUpperCase(), "Branch", CODE_PATTERN, 
                             "Branch can only contain letters and numbers");
    }
    
    /**
     * Validate city - flexible validation
     */
    public ValidationResult validateCity(String city) {
        ValidationResult emptyCheck = validateNotEmpty(city, "City");
        if (!emptyCheck.isValid()) return emptyCheck;
        
        ValidationResult lengthCheck = validateStringLength(city, "City", minStringLength, maxStringLength);
        if (!lengthCheck.isValid()) return lengthCheck;
        
        return validatePattern(city.trim(), "City", ALPHABETIC_PATTERN, 
                             "City can only contain letters and spaces");
    }
    
    /**
     * Validate quantity
     */
    public ValidationResult validateQuantity(String quantityStr) {
        return validateInteger(quantityStr, "Quantity", minQuantity, maxQuantity);
    }
    
    /**
     * Validate discount
     */
    public ValidationResult validateDiscount(String discountStr) {
        return validateDecimal(discountStr, "Discount", BigDecimal.ZERO, maxPrice);
    }
    
    /**
     * Validate revenue
     */
    public ValidationResult validateRevenue(String revenueStr) {
        return validateDecimal(revenueStr, "Revenue", BigDecimal.ZERO, maxPrice);
    }
}