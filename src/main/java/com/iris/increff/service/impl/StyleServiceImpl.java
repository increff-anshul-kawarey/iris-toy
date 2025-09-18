package com.iris.increff.service.impl;

import com.iris.increff.model.Style;
import com.iris.increff.model.StyleDTO;
import com.iris.increff.repository.StyleRepository;
import com.iris.increff.service.StyleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for managing Style entities.
 * Provides CRUD operations and business logic for Style management.
 */
@Service
public class StyleServiceImpl implements StyleService {

    private static final Logger logger = LoggerFactory.getLogger(StyleServiceImpl.class);
    private static final int BATCH_SIZE = 1000;

    @Autowired
    private StyleRepository styleRepository;

    @Override
    @Transactional
    public Style save(StyleDTO styleDTO) {
        logger.info("Saving new style with code: {}", styleDTO.getStyleCode());
        
        validateStyleDTO(styleDTO);
        normalizeStyleDTO(styleDTO);
        
        // Check if style code already exists
        if (styleRepository.findByStyleCode(styleDTO.getStyleCode()) != null) {
            String errorMsg = "Style with code '" + styleDTO.getStyleCode() + "' already exists";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        Style style = convertToEntity(styleDTO);
        Style savedStyle = styleRepository.save(style);
        logger.info("Successfully saved style with ID: {}", savedStyle.getId());
        return savedStyle;
    }

    @Override
    public Style findById(Integer id) {
        logger.debug("Finding style by ID: {}", id);
        
        if (id == null) {
            String errorMsg = "Style ID cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        Optional<Style> style = styleRepository.findById(id);
        if (style.isPresent()) {
            logger.debug("Found style with ID: {}", id);
            return style.get();
        } else {
            logger.debug("Style with ID {} not found", id);
            return null;
        }
    }

    @Override
    public Style findByStyleCode(String styleCode) {
        logger.debug("Finding style by code: {}", styleCode);
        
        if (!StringUtils.hasText(styleCode)) {
            String errorMsg = "Style code cannot be null or empty";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        String normalizedCode = normalizeString(styleCode);
        Style style = styleRepository.findByStyleCode(normalizedCode);
        
        if (style != null) {
            logger.debug("Found style with code: {}", normalizedCode);
        } else {
            logger.debug("Style with code {} not found", normalizedCode);
        }
        
        return style;
    }

    @Override
    public List<Style> findByCategory(String category) {
        logger.debug("Finding styles by category: {}", category);
        
        if (!StringUtils.hasText(category)) {
            String errorMsg = "Category cannot be null or empty";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        String normalizedCategory = normalizeString(category);
        List<Style> styles = styleRepository.findByCategory(normalizedCategory);
        logger.debug("Found {} styles for category: {}", styles.size(), normalizedCategory);
        return styles;
    }

    @Override
    public List<Style> findByBrand(String brand) {
        logger.debug("Finding styles by brand: {}", brand);
        
        if (!StringUtils.hasText(brand)) {
            String errorMsg = "Brand cannot be null or empty";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        String normalizedBrand = normalizeString(brand);
        List<Style> styles = styleRepository.findByBrand(normalizedBrand);
        logger.debug("Found {} styles for brand: {}", styles.size(), normalizedBrand);
        return styles;
    }

    @Override
    public List<Style> findAll() {
        logger.debug("Finding all styles");
        List<Style> styles = styleRepository.findAll();
        logger.debug("Found {} total styles", styles.size());
        return styles;
    }

    @Override
    @Transactional
    public Style update(Integer id, StyleDTO styleDTO) {
        logger.info("Updating style with ID: {}", id);
        
        if (id == null) {
            String errorMsg = "Style ID cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        validateStyleDTO(styleDTO);
        normalizeStyleDTO(styleDTO);
        
        Optional<Style> existingStyleOpt = styleRepository.findById(id);
        if (!existingStyleOpt.isPresent()) {
            String errorMsg = "Style with ID " + id + " not found";
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        Style existingStyle = existingStyleOpt.get();
        
        // Check if style code is being changed and if new code already exists
        if (!existingStyle.getStyleCode().equals(styleDTO.getStyleCode())) {
            Style styleWithSameCode = styleRepository.findByStyleCode(styleDTO.getStyleCode());
            if (styleWithSameCode != null && !styleWithSameCode.getId().equals(id)) {
                String errorMsg = "Style with code '" + styleDTO.getStyleCode() + "' already exists";
                logger.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
        }
        
        // Update the existing style with new data
        updateStyleFromDTO(existingStyle, styleDTO);
        Style updatedStyle = styleRepository.save(existingStyle);
        logger.info("Successfully updated style with ID: {}", id);
        return updatedStyle;
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        logger.info("Deleting style with ID: {}", id);
        
        if (id == null) {
            String errorMsg = "Style ID cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        if (!styleRepository.existsById(id)) {
            String errorMsg = "Style with ID " + id + " not found";
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        styleRepository.deleteById(id);
        logger.info("Successfully deleted style with ID: {}", id);
    }

    @Override
    @Transactional
    public List<Style> saveBatch(List<StyleDTO> styleDTOs) {
        logger.info("Starting batch save of {} styles", styleDTOs.size());
        
        if (styleDTOs == null || styleDTOs.isEmpty()) {
            String errorMsg = "StyleDTOs list cannot be null or empty";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        List<Style> savedStyles = new ArrayList<>();
        List<StyleDTO> validDTOs = new ArrayList<>();
        
        // Validate and normalize all DTOs first
        for (int i = 0; i < styleDTOs.size(); i++) {
            StyleDTO dto = styleDTOs.get(i);
            try {
                validateStyleDTO(dto);
                normalizeStyleDTO(dto);
                validDTOs.add(dto);
            } catch (IllegalArgumentException e) {
                logger.error("Validation failed for style at index {}: {}", i, e.getMessage());
                throw new RuntimeException("Validation failed for style at index " + i + ": " + e.getMessage());
            }
        }
        
        // Check for duplicate style codes in the batch
        checkForDuplicateStyleCodes(validDTOs);
        
        // Check for existing style codes in database
        checkForExistingStyleCodes(validDTOs);
        
        // Process in chunks for better performance
        for (int i = 0; i < validDTOs.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, validDTOs.size());
            List<StyleDTO> chunk = validDTOs.subList(i, endIndex);
            
            logger.debug("Processing chunk {}-{} of {} styles", i + 1, endIndex, validDTOs.size());
            
            List<Style> chunkStyles = new ArrayList<>();
            for (StyleDTO dto : chunk) {
                Style style = convertToEntity(dto);
                chunkStyles.add(style);
            }
            
            List<Style> savedChunk = styleRepository.saveAll(chunkStyles);
            savedStyles.addAll(savedChunk);
        }
        
        logger.info("Successfully saved {} styles in batch", savedStyles.size());
        return savedStyles;
    }

    /**
     * Validates a StyleDTO for required fields and constraints.
     */
    private void validateStyleDTO(StyleDTO styleDTO) {
        if (styleDTO == null) {
            throw new IllegalArgumentException("StyleDTO cannot be null");
        }
        
        if (!StringUtils.hasText(styleDTO.getStyleCode())) {
            throw new IllegalArgumentException("Style code cannot be null or empty");
        }
        
        if (!StringUtils.hasText(styleDTO.getBrand())) {
            throw new IllegalArgumentException("Brand cannot be null or empty");
        }
        
        if (!StringUtils.hasText(styleDTO.getCategory())) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
        
        if (!StringUtils.hasText(styleDTO.getSubCategory())) {
            throw new IllegalArgumentException("Sub-category cannot be null or empty");
        }
        
        if (!StringUtils.hasText(styleDTO.getGender())) {
            throw new IllegalArgumentException("Gender cannot be null or empty");
        }
        
        if (styleDTO.getMrp() == null) {
            throw new IllegalArgumentException("MRP cannot be null");
        }
        
        if (styleDTO.getMrp().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("MRP must be greater than 0");
        }
    }

    /**
     * Normalizes string fields in StyleDTO by trimming and converting to lowercase.
     */
    private void normalizeStyleDTO(StyleDTO styleDTO) {
        styleDTO.setStyleCode(normalizeString(styleDTO.getStyleCode()));
        styleDTO.setBrand(normalizeString(styleDTO.getBrand()));
        styleDTO.setCategory(normalizeString(styleDTO.getCategory()));
        styleDTO.setSubCategory(normalizeString(styleDTO.getSubCategory()));
        styleDTO.setGender(normalizeString(styleDTO.getGender()));
    }

    /**
     * Normalizes a string by trimming whitespace and converting to lowercase.
     */
    private String normalizeString(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().toLowerCase();
    }

    /**
     * Converts StyleDTO to Style entity.
     */
    private Style convertToEntity(StyleDTO styleDTO) {
        Style style = new Style();
        style.setId(styleDTO.getId());
        style.setStyleCode(styleDTO.getStyleCode());
        style.setBrand(styleDTO.getBrand());
        style.setCategory(styleDTO.getCategory());
        style.setSubCategory(styleDTO.getSubCategory());
        style.setMrp(styleDTO.getMrp());
        style.setGender(styleDTO.getGender());
        return style;
    }

    /**
     * Updates an existing Style entity with data from StyleDTO.
     */
    private void updateStyleFromDTO(Style style, StyleDTO styleDTO) {
        style.setStyleCode(styleDTO.getStyleCode());
        style.setBrand(styleDTO.getBrand());
        style.setCategory(styleDTO.getCategory());
        style.setSubCategory(styleDTO.getSubCategory());
        style.setMrp(styleDTO.getMrp());
        style.setGender(styleDTO.getGender());
    }

    /**
     * Checks for duplicate style codes within the batch.
     */
    private void checkForDuplicateStyleCodes(List<StyleDTO> styleDTOs) {
        for (int i = 0; i < styleDTOs.size(); i++) {
            for (int j = i + 1; j < styleDTOs.size(); j++) {
                if (styleDTOs.get(i).getStyleCode().equals(styleDTOs.get(j).getStyleCode())) {
                    String errorMsg = "Duplicate style code '" + styleDTOs.get(i).getStyleCode() + 
                                    "' found at indices " + i + " and " + j;
                    logger.error(errorMsg);
                    throw new IllegalArgumentException(errorMsg);
                }
            }
        }
    }

    /**
     * Checks for existing style codes in the database.
     */
    private void checkForExistingStyleCodes(List<StyleDTO> styleDTOs) {
        for (int i = 0; i < styleDTOs.size(); i++) {
            StyleDTO dto = styleDTOs.get(i);
            Style existingStyle = styleRepository.findByStyleCode(dto.getStyleCode());
            if (existingStyle != null) {
                String errorMsg = "Style with code '" + dto.getStyleCode() + "' already exists in database";
                logger.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
        }
    }
}
