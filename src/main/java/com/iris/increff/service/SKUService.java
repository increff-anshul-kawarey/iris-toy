package com.iris.increff.service;

import com.iris.increff.model.SKU;
import com.iris.increff.model.SKUDTO;
import java.util.List;

/**
 * Service interface for managing SKU entities.
 * Provides CRUD operations and business logic for SKU management.
 */
public interface SKUService {

    /**
     * Saves a new SKU entity from the provided SKUDTO.
     * 
     * @param skuDTO the SKUDTO containing the SKU information to be saved
     * @return the saved SKU entity with generated ID
     * @throws IllegalArgumentException if skuDTO is null or contains invalid data
     */
    SKU save(SKUDTO skuDTO);

    /**
     * Retrieves a SKU entity by its unique identifier.
     * 
     * @param id the unique identifier of the SKU
     * @return the SKU entity if found, null otherwise
     * @throws IllegalArgumentException if id is null
     */
    SKU findById(Integer id);

    /**
     * Retrieves a SKU entity by its unique SKU code.
     * 
     * @param sku the unique SKU code to search for
     * @return the SKU entity if found, null otherwise
     * @throws IllegalArgumentException if sku is null or empty
     */
    SKU findBySku(String sku);

    /**
     * Retrieves all SKU entities that belong to the specified style.
     * 
     * @param styleId the style ID to filter SKUs by
     * @return a list of SKU entities matching the style ID, empty list if none found
     * @throws IllegalArgumentException if styleId is null
     */
    List<SKU> findByStyleId(Integer styleId);

    /**
     * Retrieves all SKU entities from the database.
     * 
     * @return a list of all SKU entities, empty list if none found
     */
    List<SKU> findAll();

    /**
     * Updates an existing SKU entity with the provided SKUDTO data.
     * 
     * @param id the unique identifier of the SKU to update
     * @param skuDTO the SKUDTO containing the updated SKU information
     * @return the updated SKU entity
     * @throws IllegalArgumentException if id is null or skuDTO is null
     * @throws RuntimeException if SKU with given id is not found
     */
    SKU update(Integer id, SKUDTO skuDTO);

    /**
     * Deletes a SKU entity by its unique identifier.
     * 
     * @param id the unique identifier of the SKU to delete
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if SKU with given id is not found
     */
    void deleteById(Integer id);

    /**
     * Saves multiple SKU entities in a batch operation from a list of SKUDTOs.
     * This method is optimized for bulk insertions and provides better performance
     * compared to individual save operations.
     * 
     * @param skuDTOs the list of SKUDTOs to be saved
     * @return a list of saved SKU entities with generated IDs
     * @throws IllegalArgumentException if skuDTOs is null or empty
     * @throws RuntimeException if any of the SKUDTOs contain invalid data
     */
    List<SKU> saveBatch(List<SKUDTO> skuDTOs);
}
