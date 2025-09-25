package com.iris.increff.dao;

import com.iris.increff.model.Style;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Data Access Object for Style entity.
 * Handles database operations using traditional Spring + Hibernate approach.
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025-01-01
 */
@Repository
public class StyleDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Save a style to the database
     * 
     * @param style The style to save
     * @return The saved style with generated ID
     */
    public Style save(Style style) {
        if (style.getId() == null) {
            entityManager.persist(style);
        } else {
            entityManager.merge(style);
        }
        return style;
    }

    /**
     * Find a style by its ID
     * 
     * @param id The style ID
     * @return The style if found, null otherwise
     */
    public Style findById(Integer id) {
        return entityManager.find(Style.class, id);
    }

    /**
     * Find a style by its style code
     * Used for TSV processing to lookup style IDs
     * 
     * @param styleCode The style code to search for
     * @return The style if found, null otherwise
     */
    public Style findByStyleCode(String styleCode) {
        try {
            Query query = entityManager.createQuery(
                "SELECT s FROM Style s WHERE s.styleCode = :styleCode", Style.class);
            query.setParameter("styleCode", styleCode);
            return (Style) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find all styles
     * 
     * @return List of all styles
     */
    @SuppressWarnings("unchecked")
    public List<Style> findAll() {
        Query query = entityManager.createQuery("SELECT s FROM Style s");
        return query.getResultList();
    }

    /**
     * Delete all styles (for TSV replacement upload)
     */
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM Style").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE styles AUTO_INCREMENT = 1").executeUpdate();
    }

    /**
     * Save multiple styles efficiently
     * 
     * @param styles List of styles to save
     */
    public void saveAll(List<Style> styles) {
        for (int i = 0; i < styles.size(); i++) {
            entityManager.persist(styles.get(i));
            // Flush and clear every 20 entities to manage memory
            if (i % 20 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Check if a style with given style code exists
     * 
     * @param styleCode The style code to check
     * @return true if exists, false otherwise
     */
    public boolean existsByStyleCode(String styleCode) {
        Query query = entityManager.createQuery(
            "SELECT COUNT(s) FROM Style s WHERE s.styleCode = :styleCode");
        query.setParameter("styleCode", styleCode);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }
}
