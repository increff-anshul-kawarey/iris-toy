package com.iris.increff.repository;

import com.iris.increff.model.SKU;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SKURepository extends JpaRepository<SKU, Integer> {
    
    /**
     * Find SKU by SKU code
     * @param sku the SKU code to search for
     * @return SKU entity if found, null otherwise
     */
    SKU findBySku(String sku);
    
    /**
     * Find all SKUs by style ID
     * @param styleId the style ID to search for
     * @return List of SKU entities matching the style ID
     */
    List<SKU> findByStyleId(Integer styleId);
    
    /**
     * Find all SKUs by size
     * @param size the size to search for
     * @return List of SKU entities matching the size
     */
    List<SKU> findBySize(String size);
    
    /**
     * Find all SKUs by style ID and size
     * @param styleId the style ID to search for
     * @param size the size to search for
     * @return List of SKU entities matching both style ID and size
     */
    List<SKU> findByStyleIdAndSize(Integer styleId, String size);
    
    // Explicitly declare JpaRepository methods that should be inherited
    // This ensures they are available even if there are version issues
    
    /**
     * Find SKU by ID
     * @param id the ID to search for
     * @return Optional containing SKU entity if found, empty otherwise
     */
    Optional<SKU> findById(Integer id);
    
    /**
     * Save a SKU entity
     * @param sku the SKU entity to save
     * @return the saved SKU entity
     */
    <S extends SKU> S save(S sku);
    
    /**
     * Save multiple SKU entities
     * @param skus the list of SKU entities to save
     * @return list of saved SKU entities
     */
    <S extends SKU> List<S> saveAll(Iterable<S> skus);
    
    /**
     * Check if SKU exists by ID
     * @param id the ID to check
     * @return true if exists, false otherwise
     */
    boolean existsById(Integer id);
    
    /**
     * Delete SKU by ID
     * @param id the ID of the SKU to delete
     */
    void deleteById(Integer id);
    
    /**
     * Find all SKUs
     * @return list of all SKU entities
     */
    List<SKU> findAll();
}
