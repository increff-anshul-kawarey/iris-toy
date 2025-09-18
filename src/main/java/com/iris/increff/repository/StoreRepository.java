package com.iris.increff.repository;

import com.iris.increff.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Integer> {
    
    /**
     * Find all stores by city
     * @param city the city to search for
     * @return List of Store entities matching the city
     */
    List<Store> findByCity(String city);
    
    /**
     * Find all stores by branch name
     * @param branch the branch name to search for
     * @return List of Store entities matching the branch
     */
    List<Store> findByBranch(String branch);
    
    // Explicitly declare JpaRepository methods that should be inherited
    // This ensures they are available even if there are version issues
    
    /**
     * Find store by ID
     * @param id the ID to search for
     * @return Optional containing Store entity if found, empty otherwise
     */
    Optional<Store> findById(Integer id);
    
    /**
     * Save a store entity
     * @param store the store entity to save
     * @return the saved store entity
     */
    <S extends Store> S save(S store);
    
    /**
     * Save multiple store entities
     * @param stores the list of store entities to save
     * @return list of saved store entities
     */
    <S extends Store> List<S> saveAll(Iterable<S> stores);
    
    /**
     * Check if store exists by ID
     * @param id the ID to check
     * @return true if exists, false otherwise
     */
    boolean existsById(Integer id);
    
    /**
     * Delete store by ID
     * @param id the ID of the store to delete
     */
    void deleteById(Integer id);
    
    /**
     * Find all stores
     * @return list of all store entities
     */
    List<Store> findAll();
}
