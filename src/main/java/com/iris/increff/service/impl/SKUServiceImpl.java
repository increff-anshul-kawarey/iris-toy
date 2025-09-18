package com.iris.increff.service.impl;

import com.iris.increff.model.SKU;
import com.iris.increff.model.SKUDTO;
import com.iris.increff.repository.SKURepository;
import com.iris.increff.repository.StyleRepository;
import com.iris.increff.service.SKUService;
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
 * Service implementation for managing SKU entities.
 * Provides CRUD operations and business logic for SKU management.
 */
@Service
public class SKUServiceImpl implements SKUService {

    private static final Logger logger = LoggerFactory.getLogger(SKUServiceImpl.class);
    private static final int BATCH_SIZE = 1000;

    @Autowired
    private SKURepository skuRepository;

    @Autowired
    private StyleRepository styleRepository;

    @Override
    @Transactional
    public SKU save(SKUDTO skuDTO) {
        logger.info("Saving new SKU: {} for style ID: {}", skuDTO.getSku(), skuDTO.getStyleId());
        
        validateSKUDTO(skuDTO);
        normalizeSKUDTO(skuDTO);
        
        // Check if SKU code already exists
        if (skuRepository.findBySku(skuDTO.getSku()) != null) {
            String errorMsg = "SKU with code '" + skuDTO.getSku() + "' already exists";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        // Validate that style exists
        validateStyleExists(skuDTO.getStyleId());
        
        SKU sku = convertToEntity(skuDTO);
        SKU savedSku = skuRepository.save(sku);
        logger.info("Successfully saved SKU with ID: {}", savedSku.getId());
        return savedSku;
    }

    @Override
    @Transactional(readOnly = true)
    public SKU findById(Integer id) {
        logger.debug("Finding SKU by ID: {}", id);
        
        if (id == null) {
            String errorMsg = "SKU ID cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        Optional<SKU> sku = skuRepository.findById(id);
        if (sku.isPresent()) {
            logger.debug("Found SKU with ID: {}", id);
            return sku.get();
        } else {
            logger.debug("SKU with ID {} not found", id);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SKU findBySku(String sku) {
        logger.debug("Finding SKU by SKU code: {}", sku);
        
        if (!StringUtils.hasText(sku)) {
            String errorMsg = "SKU code cannot be null or empty";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        String normalizedSku = normalizeString(sku);
        SKU foundSku = skuRepository.findBySku(normalizedSku);
        
        if (foundSku != null) {
            logger.debug("Found SKU with code: {}", normalizedSku);
        } else {
            logger.debug("SKU with code {} not found", normalizedSku);
        }
        
        return foundSku;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SKU> findByStyleId(Integer styleId) {
        logger.debug("Finding SKUs by style ID: {}", styleId);
        
        if (styleId == null) {
            String errorMsg = "Style ID cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        List<SKU> skus = skuRepository.findByStyleId(styleId);
        logger.debug("Found {} SKUs for style ID: {}", skus.size(), styleId);
        return skus;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SKU> findAll() {
        logger.debug("Finding all SKUs");
        List<SKU> skus = skuRepository.findAll();
        logger.debug("Found {} total SKUs", skus.size());
        return skus;
    }

    @Override
    @Transactional
    public SKU update(Integer id, SKUDTO skuDTO) {
        logger.info("Updating SKU with ID: {}", id);
        
        if (id == null) {
            String errorMsg = "SKU ID cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        validateSKUDTO(skuDTO);
        normalizeSKUDTO(skuDTO);
        
        Optional<SKU> existingSkuOpt = skuRepository.findById(id);
        if (!existingSkuOpt.isPresent()) {
            String errorMsg = "SKU with ID " + id + " not found";
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        SKU existingSku = existingSkuOpt.get();
        
        // Check if SKU code is being changed and if new code already exists
        if (!existingSku.getSku().equals(skuDTO.getSku())) {
            SKU skuWithSameCode = skuRepository.findBySku(skuDTO.getSku());
            if (skuWithSameCode != null && !skuWithSameCode.getId().equals(id)) {
                String errorMsg = "SKU with code '" + skuDTO.getSku() + "' already exists";
                logger.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
        }
        
        // Validate that style exists
        validateStyleExists(skuDTO.getStyleId());
        
        // Update the existing SKU with new data
        updateSKUFromDTO(existingSku, skuDTO);
        SKU updatedSku = skuRepository.save(existingSku);
        logger.info("Successfully updated SKU with ID: {}", id);
        return updatedSku;
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        logger.info("Deleting SKU with ID: {}", id);
        
        if (id == null) {
            String errorMsg = "SKU ID cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        if (!skuRepository.existsById(id)) {
            String errorMsg = "SKU with ID " + id + " not found";
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        skuRepository.deleteById(id);
        logger.info("Successfully deleted SKU with ID: {}", id);
    }

    @Override
    @Transactional
    public List<SKU> saveBatch(List<SKUDTO> skuDTOs) {
        logger.info("Starting batch save of {} SKUs", skuDTOs.size());
        
        if (skuDTOs == null || skuDTOs.isEmpty()) {
            String errorMsg = "SKUDTOs list cannot be null or empty";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        List<SKU> savedSkus = new ArrayList<>();
        List<SKUDTO> validDTOs = new ArrayList<>();
        
        // Validate and normalize all DTOs first
        for (int i = 0; i < skuDTOs.size(); i++) {
            SKUDTO dto = skuDTOs.get(i);
            try {
                validateSKUDTO(dto);
                normalizeSKUDTO(dto);
                validDTOs.add(dto);
            } catch (IllegalArgumentException e) {
                logger.error("Validation failed for SKU at index {}: {}", i, e.getMessage());
                throw new RuntimeException("Validation failed for SKU at index " + i + ": " + e.getMessage());
            }
        }
        
        // Check for duplicate SKU codes in the batch
        checkForDuplicateSKUCodes(validDTOs);
        
        // Check for existing SKU codes in database
        checkForExistingSKUCodes(validDTOs);
        
        // Validate all style IDs exist
        validateAllStylesExist(validDTOs);
        
        // Process in chunks for better performance
        for (int i = 0; i < validDTOs.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, validDTOs.size());
            List<SKUDTO> chunk = validDTOs.subList(i, endIndex);
            
            logger.debug("Processing chunk {}-{} of {} SKUs", i + 1, endIndex, validDTOs.size());
            
            List<SKU> chunkSkus = new ArrayList<>();
            for (SKUDTO dto : chunk) {
                SKU sku = convertToEntity(dto);
                chunkSkus.add(sku);
            }
            
            List<SKU> savedChunk = skuRepository.saveAll(chunkSkus);
            savedSkus.addAll(savedChunk);
        }
        
        logger.info("Successfully saved {} SKUs in batch", savedSkus.size());
        return savedSkus;
    }

    /**
     * Validates a SKUDTO for required fields and constraints.
     */
    private void validateSKUDTO(SKUDTO skuDTO) {
        if (skuDTO == null) {
            throw new IllegalArgumentException("SKUDTO cannot be null");
        }
        
        if (!StringUtils.hasText(skuDTO.getSku())) {
            throw new IllegalArgumentException("SKU code cannot be null or empty");
        }
        
        if (skuDTO.getStyleId() == null) {
            throw new IllegalArgumentException("Style ID cannot be null");
        }
        
        if (!StringUtils.hasText(skuDTO.getSize())) {
            throw new IllegalArgumentException("Size cannot be null or empty");
        }
        
        if (skuDTO.getSku().length() > 50) {
            throw new IllegalArgumentException("SKU code must be 50 characters or less");
        }
        
        if (skuDTO.getSize().length() > 10) {
            throw new IllegalArgumentException("Size must be 10 characters or less");
        }
    }

    /**
     * Normalizes string fields in SKUDTO by trimming and converting to uppercase.
     */
    private void normalizeSKUDTO(SKUDTO skuDTO) {
        skuDTO.setSku(normalizeString(skuDTO.getSku()));
        skuDTO.setSize(normalizeString(skuDTO.getSize()));
    }

    /**
     * Normalizes a string by trimming whitespace and converting to uppercase.
     */
    private String normalizeString(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().toUpperCase();
    }

    /**
     * Converts SKUDTO to SKU entity.
     */
    private SKU convertToEntity(SKUDTO skuDTO) {
        SKU sku = new SKU();
        sku.setId(skuDTO.getId());
        sku.setSku(skuDTO.getSku());
        sku.setStyleId(skuDTO.getStyleId());
        sku.setSize(skuDTO.getSize());
        return sku;
    }

    /**
     * Updates an existing SKU entity with data from SKUDTO.
     */
    private void updateSKUFromDTO(SKU sku, SKUDTO skuDTO) {
        sku.setSku(skuDTO.getSku());
        sku.setStyleId(skuDTO.getStyleId());
        sku.setSize(skuDTO.getSize());
    }

    /**
     * Validates that a style with the given ID exists.
     */
    private void validateStyleExists(Integer styleId) {
        if (styleId == null) {
            throw new IllegalArgumentException("Style ID cannot be null");
        }
        
        Optional<com.iris.increff.model.Style> style = styleRepository.findById(styleId);
        if (!style.isPresent()) {
            String errorMsg = "Style with ID " + styleId + " not found";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Validates that all style IDs in the batch exist.
     */
    private void validateAllStylesExist(List<SKUDTO> skuDTOs) {
        for (int i = 0; i < skuDTOs.size(); i++) {
            SKUDTO dto = skuDTOs.get(i);
            try {
                validateStyleExists(dto.getStyleId());
            } catch (IllegalArgumentException e) {
                logger.error("Style validation failed for SKU at index {}: {}", i, e.getMessage());
                throw new RuntimeException("Style validation failed for SKU at index " + i + ": " + e.getMessage());
            }
        }
    }

    /**
     * Checks for duplicate SKU codes within the batch.
     */
    private void checkForDuplicateSKUCodes(List<SKUDTO> skuDTOs) {
        for (int i = 0; i < skuDTOs.size(); i++) {
            for (int j = i + 1; j < skuDTOs.size(); j++) {
                if (skuDTOs.get(i).getSku().equals(skuDTOs.get(j).getSku())) {
                    String errorMsg = "Duplicate SKU code '" + skuDTOs.get(i).getSku() + 
                                    "' found at indices " + i + " and " + j;
                    logger.error(errorMsg);
                    throw new IllegalArgumentException(errorMsg);
                }
            }
        }
    }

    /**
     * Checks for existing SKU codes in the database.
     */
    private void checkForExistingSKUCodes(List<SKUDTO> skuDTOs) {
        for (int i = 0; i < skuDTOs.size(); i++) {
            SKUDTO dto = skuDTOs.get(i);
            SKU existingSku = skuRepository.findBySku(dto.getSku());
            if (existingSku != null) {
                String errorMsg = "SKU with code '" + dto.getSku() + "' already exists in database";
                logger.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
        }
    }
}
