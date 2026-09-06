package com.noshop.product_service.repository;

import com.noshop.product_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    Optional<Category> findBySlug(String slug);
}