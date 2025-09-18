package com.iris.increff.service;

import com.iris.increff.model.Store;
import com.iris.increff.model.StoreDTO;
import java.util.List;

/**
 * Service interface for managing Store entities.
 * Provides CRUD operations and business logic for Store management.
 */
public interface StoreService {

    /**
     * Saves a new Store entity from the provided StoreDTO.
     * 
     * @param storeDTO the StoreDTO containing the store information to be saved
     * @return the saved Store entity with generated ID
     * @throws IllegalArgumentException if storeDTO is null or contains invalid data
     */
    Store save(StoreDTO storeDTO);

    /**
     * Retrieves a Store entity by its unique identifier.
     * 
     * @param id the unique identifier of the store
     * @return the Store entity if found, null otherwise
     * @throws IllegalArgumentException if id is null
     */
    Store findById(Integer id);

    /**
     * Retrieves all Store entities that are located in the specified city.
     * 
     * @param city the city to filter stores by
     * @return a list of Store entities matching the city, empty list if none found
     * @throws IllegalArgumentException if city is null or empty
     */
    List<Store> findByCity(String city);

    /**
     * Retrieves all Store entities from the database.
     * 
     * @return a list of all Store entities, empty list if none found
     */
    List<Store> findAll();

    /**
     * Updates an existing Store entity with the provided StoreDTO data.
     * 
     * @param id the unique identifier of the store to update
     * @param storeDTO the StoreDTO containing the updated store information
     * @return the updated Store entity
     * @throws IllegalArgumentException if id is null or storeDTO is null
     * @throws RuntimeException if store with given id is not found
     */
    Store update(Integer id, StoreDTO storeDTO);

    /**
     * Deletes a Store entity by its unique identifier.
     * 
     * @param id the unique identifier of the store to delete
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if store with given id is not found
     */
    void deleteById(Integer id);

    /**
     * Saves multiple Store entities in a batch operation from a list of StoreDTOs.
     * This method is optimized for bulk insertions and provides better performance
     * compared to individual save operations.
     * 
     * @param storeDTOs the list of StoreDTOs to be saved
     * @return a list of saved Store entities with generated IDs
     * @throws IllegalArgumentException if storeDTOs is null or empty
     * @throws RuntimeException if any of the StoreDTOs contain invalid data
     */
    List<Store> saveBatch(List<StoreDTO> storeDTOs);
}
