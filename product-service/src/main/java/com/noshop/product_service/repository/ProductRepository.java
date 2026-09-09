package com.noshop.product_service.repository;

import com.noshop.product_service.entity.Product;
import com.noshop.product_service.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySlug(String slug);

    Optional<Product> findBySlug(String slug);

    @Query("""
            SELECT DISTINCT p
            FROM Product p
            LEFT JOIN FETCH p.images
            LEFT JOIN FETCH p.brand
            LEFT JOIN FETCH p.category
            LEFT JOIN FETCH p.subCategory
            WHERE p.id = :id
            """)
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    boolean existsBySlugAndIdNot(String slug, Long id);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findBySubCategoryId(Long subCategoryId, Pageable pageable);

    Page<Product> findByCategoryIdAndSubCategoryId(
            Long categoryId,
            Long subCategoryId,
            Pageable pageable
    );

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(
            Long categoryId,
            ProductStatus status,
            Pageable pageable
    );

    Page<Product> findBySubCategoryIdAndStatus(
            Long subCategoryId,
            ProductStatus status,
            Pageable pageable
    );

    Page<Product> findByCategoryIdAndSubCategoryIdAndStatus(
            Long categoryId,
            Long subCategoryId,
            ProductStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Product p
            WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<Product> searchProducts(
            @Param("query") String query,
            Pageable pageable
    );
}
