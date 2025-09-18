package com.iris.increff.repository;

import com.iris.increff.model.Style;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StyleRepository extends JpaRepository<Style, Integer> {
    
    /**
     * Find style by style code
     * @param styleCode the style code to search for
     * @return Style entity if found, null otherwise
     */
    Style findByStyleCode(String styleCode);

    
    
    /**
     * Find all styles by brand
     * @param brand the brand to search for
     * @return List of Style entities matching the brand
     */
    List<Style> findByBrand(String brand);
    
    /**
     * Find all styles by category
     * @param category the category to search for
     * @return List of Style entities matching the category
     */
    List<Style> findByCategory(String category);
    
    /**
     * Find all styles by category and brand
     * @param category the category to search for
     * @param brand the brand to search for
     * @return List of Style entities matching both category and brand
     */
    List<Style> findByCategoryAndBrand(String category, String brand);
    
    // Explicitly declare JpaRepository methods that should be inherited
    // This ensures they are available even if there are version issues
    
    /**
     * Find style by ID
     * @param id the ID to search for
     * @return Optional containing Style entity if found, empty otherwise
     */
    Optional<Style> findById(Integer id);
    
    /**
     * Save a style entity
     * @param style the style entity to save
     * @return the saved style entity
     */
    <S extends Style> S save(S style);
    
    /**
     * Save multiple style entities
     * @param styles the list of style entities to save
     * @return list of saved style entities
     */
    <S extends Style> List<S> saveAll(Iterable<S> styles);
    
    /**
     * Check if style exists by ID
     * @param id the ID to check
     * @return true if exists, false otherwise
     */
    boolean existsById(Integer id);
    
    /**
     * Delete style by ID
     * @param id the ID of the style to delete
     */
    void deleteById(Integer id);
    
    /**
     * Find all styles
     * @return list of all style entities
     */
    List<Style> findAll();
}
