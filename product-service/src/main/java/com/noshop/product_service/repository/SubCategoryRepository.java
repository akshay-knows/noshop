package com.noshop.product_service.repository;

import com.noshop.product_service.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    Optional<SubCategory> findBySlug(String slug);

}