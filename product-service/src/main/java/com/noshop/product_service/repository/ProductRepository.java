package com.noshop.product_service.repository;

import com.noshop.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySlug(String slug);

    Optional<Product> findBySlug(String slug);

}