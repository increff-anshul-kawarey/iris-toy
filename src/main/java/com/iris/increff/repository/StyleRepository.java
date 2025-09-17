package com.iris.increff.repository;

import com.iris.increff.model.Style;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
