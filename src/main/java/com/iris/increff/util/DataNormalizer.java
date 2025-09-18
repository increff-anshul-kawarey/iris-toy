package com.iris.increff.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/**
 * Utility class for data normalization and validation.
 * Provides static methods for standardizing data formats and validating business rules.
 * Includes comprehensive input validation and error handling.
 */
public class DataNormalizer {

    private static final Logger logger = LoggerFactory.getLogger(DataNormalizer.class);

    // Constants for validation patterns
    private static final Pattern STYLE_CODE_PATTERN = Pattern.compile("^[A-Z0-9_-]{1,50}$");
    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z0-9_-]{1,50}$");
    private static final int MAX_STYLE_CODE_LENGTH = 50;
    private static final int MAX_SKU_LENGTH = 50;
    private static final int MAX_BRAND_LENGTH = 50;
    private static final int MAX_CATEGORY_LENGTH = 50;
    private static final BigDecimal MAX_DISCOUNT_PERCENTAGE = new BigDecimal("100.00");
    private static final BigDecimal MIN_MRP = new BigDecimal("0.01");
    private static final int MIN_QUANTITY = 1;

    // Private constructor to prevent instantiation
    private DataNormalizer() {
        throw new UnsupportedOperationException("DataNormalizer is a utility class and cannot be instantiated");
    }

    // ==================== STRING NORMALIZATION METHODS ====================

    /**
     * Normalizes a string by trimming whitespace and converting to lowercase.
     * This is the default normalization method for most string fields.
     * 
     * @param input the string to normalize
     * @return the normalized string, or null if input is null
     */
    public static String normalize(String input) {
        if (input == null) {
            logger.debug("Input string is null, returning null");
            return null;
        }

        String normalized = input.trim().toLowerCase();
        logger.debug("Normalized string: '{}' -> '{}'", input, normalized);
        return normalized;
    }

    /**
     * Normalizes a style code by trimming whitespace and converting to uppercase.
     * Style codes are typically used as unique identifiers and should be uppercase.
     * 
     * @param styleCode the style code to normalize
     * @return the normalized style code, or null if input is null
     * @throws IllegalArgumentException if styleCode is empty after trimming
     */
    public static String normalizeStyleCode(String styleCode) {
        if (styleCode == null) {
            logger.debug("Style code is null, returning null");
            return null;
        }

        String trimmed = styleCode.trim();
        if (trimmed.isEmpty()) {
            String errorMsg = "Style code cannot be empty";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        String normalized = trimmed.toUpperCase();
        logger.debug("Normalized style code: '{}' -> '{}'", styleCode, normalized);
        return normalized;
    }

    /**
     * Normalizes a brand name by trimming whitespace and converting to title case.
     * Title case capitalizes the first letter of each word.
     * 
     * @param brand the brand name to normalize
     * @return the normalized brand name, or null if input is null
     * @throws IllegalArgumentException if brand is empty after trimming
     */
    public static String normalizeBrand(String brand) {
        if (brand == null) {
            logger.debug("Brand is null, returning null");
            return null;
        }

        String trimmed = brand.trim();
        if (trimmed.isEmpty()) {
            String errorMsg = "Brand cannot be empty";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        String normalized = toTitleCase(trimmed);
        logger.debug("Normalized brand: '{}' -> '{}'", brand, normalized);
        return normalized;
    }

    /**
     * Normalizes a category by trimming whitespace and converting to uppercase.
     * Categories are typically used for grouping and should be uppercase.
     * 
     * @param category the category to normalize
     * @return the normalized category, or null if input is null
     * @throws IllegalArgumentException if category is empty after trimming
     */
    public static String normalizeCategory(String category) {
        if (category == null) {
            logger.debug("Category is null, returning null");
            return null;
        }

        String trimmed = category.trim();
        if (trimmed.isEmpty()) {
            String errorMsg = "Category cannot be empty";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        String normalized = trimmed.toUpperCase();
        logger.debug("Normalized category: '{}' -> '{}'", category, normalized);
        return normalized;
    }

    // ==================== NUMERIC NORMALIZATION METHODS ====================

    /**
     * Normalizes MRP (Maximum Retail Price) by ensuring it's positive and properly rounded.
     * MRP must be greater than 0 and is rounded to 2 decimal places.
     * 
     * @param mrp the MRP value to normalize
     * @return the normalized MRP, or null if input is null
     * @throws IllegalArgumentException if MRP is not positive
     */
    public static BigDecimal normalizeMRP(BigDecimal mrp) {
        if (mrp == null) {
            logger.debug("MRP is null, returning null");
            return null;
        }

        if (mrp.compareTo(BigDecimal.ZERO) <= 0) {
            String errorMsg = "MRP must be positive, got: " + mrp;
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        BigDecimal normalized = mrp.setScale(2, RoundingMode.HALF_UP);
        logger.debug("Normalized MRP: {} -> {}", mrp, normalized);
        return normalized;
    }

    /**
     * Normalizes quantity by ensuring it's positive.
     * Quantity must be greater than 0.
     * 
     * @param quantity the quantity to normalize
     * @return the normalized quantity, or null if input is null
     * @throws IllegalArgumentException if quantity is not positive
     */
    public static Integer normalizeQuantity(Integer quantity) {
        if (quantity == null) {
            logger.debug("Quantity is null, returning null");
            return null;
        }

        if (quantity <= 0) {
            String errorMsg = "Quantity must be positive, got: " + quantity;
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        logger.debug("Normalized quantity: {} -> {}", quantity, quantity);
        return quantity;
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates a style code format and length.
     * Style codes should be uppercase alphanumeric with underscores and hyphens allowed.
     * 
     * @param styleCode the style code to validate
     * @return true if the style code is valid, false otherwise
     */
    public static boolean isValidStyleCode(String styleCode) {
        if (styleCode == null) {
            logger.debug("Style code is null, validation failed");
            return false;
        }

        String trimmed = styleCode.trim();
        if (trimmed.isEmpty()) {
            logger.debug("Style code is empty, validation failed");
            return false;
        }

        if (trimmed.length() > MAX_STYLE_CODE_LENGTH) {
            logger.debug("Style code '{}' exceeds maximum length of {}", trimmed, MAX_STYLE_CODE_LENGTH);
            return false;
        }

        boolean isValid = STYLE_CODE_PATTERN.matcher(trimmed).matches();
        logger.debug("Style code '{}' validation result: {}", trimmed, isValid);
        return isValid;
    }

    /**
     * Validates an SKU format and length.
     * SKUs should be uppercase alphanumeric with underscores and hyphens allowed.
     * 
     * @param sku the SKU to validate
     * @return true if the SKU is valid, false otherwise
     */
    public static boolean isValidSKU(String sku) {
        if (sku == null) {
            logger.debug("SKU is null, validation failed");
            return false;
        }

        String trimmed = sku.trim();
        if (trimmed.isEmpty()) {
            logger.debug("SKU is empty, validation failed");
            return false;
        }

        if (trimmed.length() > MAX_SKU_LENGTH) {
            logger.debug("SKU '{}' exceeds maximum length of {}", trimmed, MAX_SKU_LENGTH);
            return false;
        }

        boolean isValid = SKU_PATTERN.matcher(trimmed).matches();
        logger.debug("SKU '{}' validation result: {}", trimmed, isValid);
        return isValid;
    }

    /**
     * Validates a discount percentage.
     * Discount should be between 0% and 100% (inclusive).
     * 
     * @param discount the discount percentage to validate
     * @return true if the discount is valid, false otherwise
     */
    public static boolean isValidDiscount(BigDecimal discount) {
        if (discount == null) {
            logger.debug("Discount is null, validation failed");
            return false;
        }

        boolean isValid = discount.compareTo(BigDecimal.ZERO) >= 0 && 
                         discount.compareTo(MAX_DISCOUNT_PERCENTAGE) <= 0;
        
        logger.debug("Discount {} validation result: {}", discount, isValid);
        return isValid;
    }

    /**
     * Validates a brand name format and length.
     * 
     * @param brand the brand name to validate
     * @return true if the brand is valid, false otherwise
     */
    public static boolean isValidBrand(String brand) {
        if (brand == null) {
            logger.debug("Brand is null, validation failed");
            return false;
        }

        String trimmed = brand.trim();
        if (trimmed.isEmpty()) {
            logger.debug("Brand is empty, validation failed");
            return false;
        }

        boolean isValid = trimmed.length() <= MAX_BRAND_LENGTH;
        logger.debug("Brand '{}' validation result: {}", trimmed, isValid);
        return isValid;
    }

    /**
     * Validates a category name format and length.
     * 
     * @param category the category name to validate
     * @return true if the category is valid, false otherwise
     */
    public static boolean isValidCategory(String category) {
        if (category == null) {
            logger.debug("Category is null, validation failed");
            return false;
        }

        String trimmed = category.trim();
        if (trimmed.isEmpty()) {
            logger.debug("Category is empty, validation failed");
            return false;
        }

        boolean isValid = trimmed.length() <= MAX_CATEGORY_LENGTH;
        logger.debug("Category '{}' validation result: {}", trimmed, isValid);
        return isValid;
    }

    /**
     * Validates an MRP value.
     * MRP must be positive and greater than the minimum threshold.
     * 
     * @param mrp the MRP value to validate
     * @return true if the MRP is valid, false otherwise
     */
    public static boolean isValidMRP(BigDecimal mrp) {
        if (mrp == null) {
            logger.debug("MRP is null, validation failed");
            return false;
        }

        boolean isValid = mrp.compareTo(MIN_MRP) >= 0;
        logger.debug("MRP {} validation result: {}", mrp, isValid);
        return isValid;
    }

    /**
     * Validates a quantity value.
     * Quantity must be positive.
     * 
     * @param quantity the quantity to validate
     * @return true if the quantity is valid, false otherwise
     */
    public static boolean isValidQuantity(Integer quantity) {
        if (quantity == null) {
            logger.debug("Quantity is null, validation failed");
            return false;
        }

        boolean isValid = quantity >= MIN_QUANTITY;
        logger.debug("Quantity {} validation result: {}", quantity, isValid);
        return isValid;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Converts a string to title case (first letter of each word capitalized).
     * 
     * @param input the string to convert
     * @return the title case string
     */
    private static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                result.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }

        return result.toString();
    }

    /**
     * Safely normalizes a string, returning null if input is null or empty after trimming.
     * 
     * @param input the string to normalize
     * @return the normalized string, or null if input is null or empty
     */
    public static String safeNormalize(String input) {
        if (input == null) {
            return null;
        }

        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }

    /**
     * Validates that an object is not null and throws IllegalArgumentException if it is.
     * 
     * @param obj the object to validate
     * @param objectName the name of the object for error messages
     * @throws IllegalArgumentException if obj is null
     */
    private static void validateNotNull(Object obj, String objectName) {
        if (obj == null) {
            String errorMsg = objectName + " cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }
}
