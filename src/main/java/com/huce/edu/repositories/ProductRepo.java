package com.huce.edu.repositories;


import com.huce.edu.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<ProductEntity, Integer> {
    ProductEntity findFirstByPid(Integer pid);
    ProductEntity findFirstByName(String name);
    List<ProductEntity> findByIsDeletedAndRemainAfter(Boolean isDeleted, Integer remain);
    boolean existsByName(String name);
}

