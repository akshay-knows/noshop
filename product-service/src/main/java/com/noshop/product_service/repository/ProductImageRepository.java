package com.noshop.product_service.repository;

import com.noshop.product_service.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository
        extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(
            Long productId
    );

    List<ProductImage> findByProductIdInOrderByDisplayOrderAsc(
            List<Long> productIds
    );

    Optional<ProductImage> findByProductIdAndDisplayOrder(
            Long productId,
            Integer displayOrder
    );

    boolean existsByProductIdAndDisplayOrder(
            Long productId,
            Integer displayOrder
    );
}