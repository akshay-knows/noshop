package com.noshop.product_service.repository;

import com.noshop.product_service.entity.Product;
import com.noshop.product_service.enums.CatalogAudience;
import com.noshop.product_service.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Query("""
            SELECT p
            FROM Product p
            WHERE (p.audience IN :audiences OR p.audience IS NULL)
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:subCategoryId IS NULL OR p.subCategory.id = :subCategoryId)
              AND (:status IS NULL OR p.status = :status)
            """)
    Page<Product> findCatalogProducts(
            @Param("audiences") Set<CatalogAudience> audiences,
            @Param("categoryId") Long categoryId,
            @Param("subCategoryId") Long subCategoryId,
            @Param("status") ProductStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT p.id AS id,
                   p.name AS name,
                   p.slug AS slug,
                   MIN(v.price) AS price
            FROM Product p
            JOIN p.variants v
            WHERE (p.audience IN :audiences OR p.audience IS NULL)
              AND p.id <> :productId
              AND p.category.id = :categoryId
              AND p.status = :activeStatus
              AND v.status = :activeStatus
              AND v.price IS NOT NULL
            GROUP BY p.id, p.name, p.slug
            """)
    List<ProductRecommendationCandidate> findRecommendationCandidates(
            @Param("audiences") Set<CatalogAudience> audiences,
            @Param("categoryId") Long categoryId,
            @Param("productId") Long productId,
            @Param("activeStatus") ProductStatus activeStatus
    );

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
            WHERE (p.audience IN :audiences OR p.audience IS NULL)
              AND (
                    LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            """)
    Page<Product> searchCatalogProducts(
            @Param("audiences") Set<CatalogAudience> audiences,
            @Param("query") String query,
            Pageable pageable
    );
}