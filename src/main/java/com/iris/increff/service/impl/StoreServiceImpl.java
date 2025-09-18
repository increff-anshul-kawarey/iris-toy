package com.iris.increff.service.impl;

import com.iris.increff.model.Store;
import com.iris.increff.model.StoreDTO;
import com.iris.increff.repository.StoreRepository;
import com.iris.increff.service.StoreService;
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
 * Service implementation for managing Store entities.
 * Provides CRUD operations and business logic for Store management.
 */
@Service
public class StoreServiceImpl implements StoreService {

    private static final Logger logger = LoggerFactory.getLogger(StoreServiceImpl.class);
    private static final int BATCH_SIZE = 1000;

    @Autowired
    private StoreRepository storeRepository;

    @Override
    @Transactional
    public Store save(StoreDTO storeDTO) {
        logger.info("Saving new store: {} in {}", storeDTO.getBranch(), storeDTO.getCity());
        
        validateStoreDTO(storeDTO);
        normalizeStoreDTO(storeDTO);
        
        // Check if store with same branch and city already exists
        if (storeExists(storeDTO.getBranch(), storeDTO.getCity())) {
            String errorMsg = "Store with branch '" + storeDTO.getBranch() + "' in city '" + storeDTO.getCity() + "' already exists";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        Store store = convertToEntity(storeDTO);
        Store savedStore = storeRepository.save(store);
        logger.info("Successfully saved store with ID: {}", savedStore.getId());
        return savedStore;
    }

    @Override
    @Transactional(readOnly = true)
    public Store findById(Integer id) {
        logger.debug("Finding store by ID: {}", id);
        
        if (id == null) {
            String errorMsg = "Store ID cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        Optional<Store> store = storeRepository.findById(id);
        if (store.isPresent()) {
            logger.debug("Found store with ID: {}", id);
            return store.get();
        } else {
            logger.debug("Store with ID {} not found", id);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Store> findByCity(String city) {
        logger.debug("Finding stores by city: {}", city);
        
        if (!StringUtils.hasText(city)) {
            String errorMsg = "City cannot be null or empty";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        String normalizedCity = normalizeString(city);
        List<Store> stores = storeRepository.findByCity(normalizedCity);
        logger.debug("Found {} stores in city: {}", stores.size(), normalizedCity);
        return stores;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Store> findAll() {
        logger.debug("Finding all stores");
        List<Store> stores = storeRepository.findAll();
        logger.debug("Found {} total stores", stores.size());
        return stores;
    }

    @Override
    @Transactional
    public Store update(Integer id, StoreDTO storeDTO) {
        logger.info("Updating store with ID: {}", id);
        
        if (id == null) {
            String errorMsg = "Store ID cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        validateStoreDTO(storeDTO);
        normalizeStoreDTO(storeDTO);
        
        Optional<Store> existingStoreOpt = storeRepository.findById(id);
        if (!existingStoreOpt.isPresent()) {
            String errorMsg = "Store with ID " + id + " not found";
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        Store existingStore = existingStoreOpt.get();
        
        // Check if the new branch and city combination already exists (excluding current store)
        if (!existingStore.getBranch().equals(storeDTO.getBranch()) || 
            !existingStore.getCity().equals(storeDTO.getCity())) {
            if (storeExists(storeDTO.getBranch(), storeDTO.getCity())) {
                String errorMsg = "Store with branch '" + storeDTO.getBranch() + "' in city '" + storeDTO.getCity() + "' already exists";
                logger.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
        }
        
        // Update the existing store with new data
        updateStoreFromDTO(existingStore, storeDTO);
        Store updatedStore = storeRepository.save(existingStore);
        logger.info("Successfully updated store with ID: {}", id);
        return updatedStore;
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        logger.info("Deleting store with ID: {}", id);
        
        if (id == null) {
            String errorMsg = "Store ID cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        if (!storeRepository.existsById(id)) {
            String errorMsg = "Store with ID " + id + " not found";
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        storeRepository.deleteById(id);
        logger.info("Successfully deleted store with ID: {}", id);
    }

    @Override
    @Transactional
    public List<Store> saveBatch(List<StoreDTO> storeDTOs) {
        logger.info("Starting batch save of {} stores", storeDTOs.size());
        
        if (storeDTOs == null || storeDTOs.isEmpty()) {
            String errorMsg = "StoreDTOs list cannot be null or empty";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        List<Store> savedStores = new ArrayList<>();
        List<StoreDTO> validDTOs = new ArrayList<>();
        
        // Validate and normalize all DTOs first
        for (int i = 0; i < storeDTOs.size(); i++) {
            StoreDTO dto = storeDTOs.get(i);
            try {
                validateStoreDTO(dto);
                normalizeStoreDTO(dto);
                validDTOs.add(dto);
            } catch (IllegalArgumentException e) {
                logger.error("Validation failed for store at index {}: {}", i, e.getMessage());
                throw new RuntimeException("Validation failed for store at index " + i + ": " + e.getMessage());
            }
        }
        
        // Check for duplicate store combinations in the batch
        checkForDuplicateStores(validDTOs);
        
        // Check for existing store combinations in database
        checkForExistingStores(validDTOs);
        
        // Process in chunks for better performance
        for (int i = 0; i < validDTOs.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, validDTOs.size());
            List<StoreDTO> chunk = validDTOs.subList(i, endIndex);
            
            logger.debug("Processing chunk {}-{} of {} stores", i + 1, endIndex, validDTOs.size());
            
            List<Store> chunkStores = new ArrayList<>();
            for (StoreDTO dto : chunk) {
                Store store = convertToEntity(dto);
                chunkStores.add(store);
            }
            
            List<Store> savedChunk = storeRepository.saveAll(chunkStores);
            savedStores.addAll(savedChunk);
        }
        
        logger.info("Successfully saved {} stores in batch", savedStores.size());
        return savedStores;
    }

    /**
     * Validates a StoreDTO for required fields and constraints.
     */
    private void validateStoreDTO(StoreDTO storeDTO) {
        if (storeDTO == null) {
            throw new IllegalArgumentException("StoreDTO cannot be null");
        }
        
        if (!StringUtils.hasText(storeDTO.getBranch())) {
            throw new IllegalArgumentException("Branch cannot be null or empty");
        }
        
        if (!StringUtils.hasText(storeDTO.getCity())) {
            throw new IllegalArgumentException("City cannot be null or empty");
        }
        
        if (storeDTO.getBranch().length() > 50) {
            throw new IllegalArgumentException("Branch must be 50 characters or less");
        }
        
        if (storeDTO.getCity().length() > 50) {
            throw new IllegalArgumentException("City must be 50 characters or less");
        }
    }

    /**
     * Normalizes string fields in StoreDTO by trimming and converting to lowercase.
     */
    private void normalizeStoreDTO(StoreDTO storeDTO) {
        storeDTO.setBranch(normalizeString(storeDTO.getBranch()));
        storeDTO.setCity(normalizeString(storeDTO.getCity()));
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
     * Converts StoreDTO to Store entity.
     */
    private Store convertToEntity(StoreDTO storeDTO) {
        Store store = new Store();
        store.setId(storeDTO.getId());
        store.setBranch(storeDTO.getBranch());
        store.setCity(storeDTO.getCity());
        return store;
    }

    /**
     * Updates an existing Store entity with data from StoreDTO.
     */
    private void updateStoreFromDTO(Store store, StoreDTO storeDTO) {
        store.setBranch(storeDTO.getBranch());
        store.setCity(storeDTO.getCity());
    }

    /**
     * Checks if a store with the given branch and city combination already exists.
     */
    private boolean storeExists(String branch, String city) {
        List<Store> existingStores = storeRepository.findByBranch(branch);
        return existingStores.stream()
            .anyMatch(store -> store.getCity().equals(city));
    }

    /**
     * Checks for duplicate store combinations within the batch.
     */
    private void checkForDuplicateStores(List<StoreDTO> storeDTOs) {
        for (int i = 0; i < storeDTOs.size(); i++) {
            for (int j = i + 1; j < storeDTOs.size(); j++) {
                StoreDTO dto1 = storeDTOs.get(i);
                StoreDTO dto2 = storeDTOs.get(j);
                if (dto1.getBranch().equals(dto2.getBranch()) && dto1.getCity().equals(dto2.getCity())) {
                    String errorMsg = "Duplicate store combination '" + dto1.getBranch() + "' in '" + dto1.getCity() + 
                                    "' found at indices " + i + " and " + j;
                    logger.error(errorMsg);
                    throw new RuntimeException(errorMsg);
                }
            }
        }
    }

    /**
     * Checks for existing store combinations in the database.
     */
    private void checkForExistingStores(List<StoreDTO> storeDTOs) {
        for (int i = 0; i < storeDTOs.size(); i++) {
            StoreDTO dto = storeDTOs.get(i);
            if (storeExists(dto.getBranch(), dto.getCity())) {
                String errorMsg = "Store with branch '" + dto.getBranch() + "' in city '" + dto.getCity() + "' already exists in database";
                logger.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        }
    }
}
