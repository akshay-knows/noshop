package com.noshop.product_service.repository;

import com.noshop.product_service.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SizeRepository extends JpaRepository<Size, Long> {

    boolean existsByName(String name);

    Optional<Size> findByName(String name);

}