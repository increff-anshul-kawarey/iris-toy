package com.iris.increff.util;

import com.iris.increff.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for converting between entities and DTOs.
 * Provides static methods for bidirectional conversion between all entity-DTO pairs.
 * Includes null checks and proper error handling.
 */
public class DataMapper {

    private static final Logger logger = LoggerFactory.getLogger(DataMapper.class);

    // Private constructor to prevent instantiation
    private DataMapper() {
        throw new UnsupportedOperationException("DataMapper is a utility class and cannot be instantiated");
    }

    // ==================== STYLE CONVERSION METHODS ====================

    /**
     * Converts StyleDTO to Style entity.
     * 
     * @param dto the StyleDTO to convert
     * @return the converted Style entity
     * @throws IllegalArgumentException if dto is null
     */
    public static Style toEntity(StyleDTO dto) {
        if (dto == null) {
            logger.error("Cannot convert null StyleDTO to entity");
            throw new IllegalArgumentException("StyleDTO cannot be null");
        }

        logger.debug("Converting StyleDTO to Style entity for style code: {}", dto.getStyleCode());

        Style entity = new Style();
        entity.setId(dto.getId());
        entity.setStyleCode(dto.getStyleCode());
        entity.setBrand(dto.getBrand());
        entity.setCategory(dto.getCategory());
        entity.setSubCategory(dto.getSubCategory());
        entity.setMrp(dto.getMrp());
        entity.setGender(dto.getGender());

        return entity;
    }

    /**
     * Converts Style entity to StyleDTO.
     * 
     * @param entity the Style entity to convert
     * @return the converted StyleDTO
     * @throws IllegalArgumentException if entity is null
     */
    public static StyleDTO toDTO(Style entity) {
        if (entity == null) {
            logger.error("Cannot convert null Style entity to DTO");
            throw new IllegalArgumentException("Style entity cannot be null");
        }

        logger.debug("Converting Style entity to StyleDTO for style code: {}", entity.getStyleCode());

        StyleDTO dto = new StyleDTO();
        dto.setId(entity.getId());
        dto.setStyleCode(entity.getStyleCode());
        dto.setBrand(entity.getBrand());
        dto.setCategory(entity.getCategory());
        dto.setSubCategory(entity.getSubCategory());
        dto.setMrp(entity.getMrp());
        dto.setGender(entity.getGender());

        return dto;
    }

    /**
     * Converts a list of Style entities to a list of StyleDTOs.
     * 
     * @param entities the list of Style entities to convert
     * @return the converted list of StyleDTOs
     * @throws IllegalArgumentException if entities is null
     */
    public static List<StyleDTO> toStyleDTOList(List<Style> entities) {
        if (entities == null) {
            logger.error("Cannot convert null Style entities list to DTOs");
            throw new IllegalArgumentException("Style entities list cannot be null");
        }

        logger.debug("Converting {} Style entities to DTOs", entities.size());

        return entities.stream()
                .map(DataMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== SKU CONVERSION METHODS ====================

    /**
     * Converts SKUDTO to SKU entity.
     * 
     * @param dto the SKUDTO to convert
     * @return the converted SKU entity
     * @throws IllegalArgumentException if dto is null
     */
    public static SKU toEntity(SKUDTO dto) {
        if (dto == null) {
            logger.error("Cannot convert null SKUDTO to entity");
            throw new IllegalArgumentException("SKUDTO cannot be null");
        }

        logger.debug("Converting SKUDTO to SKU entity for SKU: {}", dto.getSku());

        SKU entity = new SKU();
        entity.setId(dto.getId());
        entity.setSku(dto.getSku());
        entity.setStyleId(dto.getStyleId());
        entity.setSize(dto.getSize());

        return entity;
    }

    /**
     * Converts SKU entity to SKUDTO.
     * 
     * @param entity the SKU entity to convert
     * @return the converted SKUDTO
     * @throws IllegalArgumentException if entity is null
     */
    public static SKUDTO toDTO(SKU entity) {
        if (entity == null) {
            logger.error("Cannot convert null SKU entity to DTO");
            throw new IllegalArgumentException("SKU entity cannot be null");
        }

        logger.debug("Converting SKU entity to SKUDTO for SKU: {}", entity.getSku());

        SKUDTO dto = new SKUDTO();
        dto.setId(entity.getId());
        dto.setSku(entity.getSku());
        dto.setStyleId(entity.getStyleId());
        dto.setSize(entity.getSize());

        return dto;
    }

    /**
     * Converts a list of SKU entities to a list of SKUDTOs.
     * 
     * @param entities the list of SKU entities to convert
     * @return the converted list of SKUDTOs
     * @throws IllegalArgumentException if entities is null
     */
    public static List<SKUDTO> toSKUDTOList(List<SKU> entities) {
        if (entities == null) {
            logger.error("Cannot convert null SKU entities list to DTOs");
            throw new IllegalArgumentException("SKU entities list cannot be null");
        }

        logger.debug("Converting {} SKU entities to DTOs", entities.size());

        return entities.stream()
                .map(DataMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== SALES CONVERSION METHODS ====================

    /**
     * Converts SalesDTO to Sales entity.
     * 
     * @param dto the SalesDTO to convert
     * @return the converted Sales entity
     * @throws IllegalArgumentException if dto is null
     */
    public static Sales toEntity(SalesDTO dto) {
        if (dto == null) {
            logger.error("Cannot convert null SalesDTO to entity");
            throw new IllegalArgumentException("SalesDTO cannot be null");
        }

        logger.debug("Converting SalesDTO to Sales entity for SKU ID: {}, Store ID: {}", dto.getSkuId(), dto.getStoreId());

        Sales entity = new Sales();
        entity.setId(dto.getId());
        entity.setDate(dto.getDate());
        entity.setSkuId(dto.getSkuId());
        entity.setStoreId(dto.getStoreId());
        entity.setQuantity(dto.getQuantity());
        entity.setDiscount(dto.getDiscount());
        entity.setRevenue(dto.getRevenue());

        return entity;
    }

    /**
     * Converts Sales entity to SalesDTO.
     * 
     * @param entity the Sales entity to convert
     * @return the converted SalesDTO
     * @throws IllegalArgumentException if entity is null
     */
    public static SalesDTO toDTO(Sales entity) {
        if (entity == null) {
            logger.error("Cannot convert null Sales entity to DTO");
            throw new IllegalArgumentException("Sales entity cannot be null");
        }

        logger.debug("Converting Sales entity to SalesDTO for SKU ID: {}, Store ID: {}", entity.getSkuId(), entity.getStoreId());

        SalesDTO dto = new SalesDTO();
        dto.setId(entity.getId());
        dto.setDate(entity.getDate());
        dto.setSkuId(entity.getSkuId());
        dto.setStoreId(entity.getStoreId());
        dto.setQuantity(entity.getQuantity());
        dto.setDiscount(entity.getDiscount());
        dto.setRevenue(entity.getRevenue());

        return dto;
    }

    /**
     * Converts a list of Sales entities to a list of SalesDTOs.
     * 
     * @param entities the list of Sales entities to convert
     * @return the converted list of SalesDTOs
     * @throws IllegalArgumentException if entities is null
     */
    public static List<SalesDTO> toSalesDTOList(List<Sales> entities) {
        if (entities == null) {
            logger.error("Cannot convert null Sales entities list to DTOs");
            throw new IllegalArgumentException("Sales entities list cannot be null");
        }

        logger.debug("Converting {} Sales entities to DTOs", entities.size());

        return entities.stream()
                .map(DataMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== STORE CONVERSION METHODS ====================

    /**
     * Converts StoreDTO to Store entity.
     * 
     * @param dto the StoreDTO to convert
     * @return the converted Store entity
     * @throws IllegalArgumentException if dto is null
     */
    public static Store toEntity(StoreDTO dto) {
        if (dto == null) {
            logger.error("Cannot convert null StoreDTO to entity");
            throw new IllegalArgumentException("StoreDTO cannot be null");
        }

        logger.debug("Converting StoreDTO to Store entity for branch: {}, city: {}", dto.getBranch(), dto.getCity());

        Store entity = new Store();
        entity.setId(dto.getId());
        entity.setBranch(dto.getBranch());
        entity.setCity(dto.getCity());

        return entity;
    }

    /**
     * Converts Store entity to StoreDTO.
     * 
     * @param entity the Store entity to convert
     * @return the converted StoreDTO
     * @throws IllegalArgumentException if entity is null
     */
    public static StoreDTO toDTO(Store entity) {
        if (entity == null) {
            logger.error("Cannot convert null Store entity to DTO");
            throw new IllegalArgumentException("Store entity cannot be null");
        }

        logger.debug("Converting Store entity to StoreDTO for branch: {}, city: {}", entity.getBranch(), entity.getCity());

        StoreDTO dto = new StoreDTO();
        dto.setId(entity.getId());
        dto.setBranch(entity.getBranch());
        dto.setCity(entity.getCity());

        return dto;
    }

    /**
     * Converts a list of Store entities to a list of StoreDTOs.
     * 
     * @param entities the list of Store entities to convert
     * @return the converted list of StoreDTOs
     * @throws IllegalArgumentException if entities is null
     */
    public static List<StoreDTO> toStoreDTOList(List<Store> entities) {
        if (entities == null) {
            logger.error("Cannot convert null Store entities list to DTOs");
            throw new IllegalArgumentException("Store entities list cannot be null");
        }

        logger.debug("Converting {} Store entities to DTOs", entities.size());

        return entities.stream()
                .map(DataMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Safely converts a list of entities to DTOs, handling null lists gracefully.
     * 
     * @param entities the list of entities to convert
     * @param converter the conversion function
     * @param <T> the entity type
     * @param <R> the DTO type
     * @return the converted list, or empty list if input is null
     */
    public static <T, R> List<R> safeConvertList(List<T> entities, java.util.function.Function<T, R> converter) {
        if (entities == null) {
            logger.debug("Input entities list is null, returning empty list");
            return new ArrayList<>();
        }

        logger.debug("Safely converting {} entities to DTOs", entities.size());

        return entities.stream()
                .filter(entity -> entity != null)
                .map(converter)
                .collect(Collectors.toList());
    }

    /**
     * Validates that an object is not null and throws IllegalArgumentException if it is.
     * 
     * @param obj the object to validate
     * @param objectName the name of the object for error messages
     * @throws IllegalArgumentException if obj is null
     */
    private static void validateNotNull(Object obj, String objectName) {
        if (obj == null) {
            String errorMsg = objectName + " cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }
}
