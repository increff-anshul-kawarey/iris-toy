package com.iris.increff.service;

import com.iris.increff.model.Style;
import com.iris.increff.model.StyleDTO;
import java.util.List;

/**
 * Service interface for managing Style entities.
 * Provides CRUD operations and business logic for Style management.
 */
public interface StyleService {

    /**
     * Saves a new Style entity from the provided StyleDTO.
     * 
     * @param styleDTO the StyleDTO containing the style information to be saved
     * @return the saved Style entity with generated ID
     * @throws IllegalArgumentException if styleDTO is null or contains invalid data
     */
    Style save(StyleDTO styleDTO);

    /**
     * Retrieves a Style entity by its unique identifier.
     * 
     * @param id the unique identifier of the style
     * @return the Style entity if found, null otherwise
     * @throws IllegalArgumentException if id is null
     */
    Style findById(Integer id);

    /**
     * Retrieves a Style entity by its unique style code.
     * 
     * @param styleCode the unique style code to search for
     * @return the Style entity if found, null otherwise
     * @throws IllegalArgumentException if styleCode is null or empty
     */
    Style findByStyleCode(String styleCode);

    /**
     * Retrieves all Style entities that belong to the specified category.
     * 
     * @param category the category to filter styles by
     * @return a list of Style entities matching the category, empty list if none found
     * @throws IllegalArgumentException if category is null or empty
     */
    List<Style> findByCategory(String category);

    /**
     * Retrieves all Style entities that belong to the specified brand.
     * 
     * @param brand the brand to filter styles by
     * @return a list of Style entities matching the brand, empty list if none found
     * @throws IllegalArgumentException if brand is null or empty
     */
    List<Style> findByBrand(String brand);

    /**
     * Retrieves all Style entities from the database.
     * 
     * @return a list of all Style entities, empty list if none found
     */
    List<Style> findAll();

    /**
     * Updates an existing Style entity with the provided StyleDTO data.
     * 
     * @param id the unique identifier of the style to update
     * @param styleDTO the StyleDTO containing the updated style information
     * @return the updated Style entity
     * @throws IllegalArgumentException if id is null or styleDTO is null
     * @throws RuntimeException if style with given id is not found
     */
    Style update(Integer id, StyleDTO styleDTO);

    /**
     * Deletes a Style entity by its unique identifier.
     * 
     * @param id the unique identifier of the style to delete
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if style with given id is not found
     */
    void deleteById(Integer id);

    /**
     * Saves multiple Style entities in a batch operation from a list of StyleDTOs.
     * This method is optimized for bulk insertions and provides better performance
     * compared to individual save operations.
     * 
     * @param styleDTOs the list of StyleDTOs to be saved
     * @return a list of saved Style entities with generated IDs
     * @throws IllegalArgumentException if styleDTOs is null or empty
     * @throws RuntimeException if any of the StyleDTOs contain invalid data
     */
    List<Style> saveBatch(List<StyleDTO> styleDTOs);
}
