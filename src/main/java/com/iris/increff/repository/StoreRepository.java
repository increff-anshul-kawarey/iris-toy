package com.iris.increff.repository;

import com.iris.increff.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
