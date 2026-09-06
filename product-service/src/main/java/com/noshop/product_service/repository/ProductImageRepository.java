//package com.noshop.product_service.repository;
//
//import com.noshop.product_service.entity.ProductImage;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//
//public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
//
//    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);
//
//}