package com.noshop.product_service.controller;

import com.noshop.product_service.dto.request.CreateSubCategoryRequest;
import com.noshop.product_service.dto.response.SubCategoryResponse;
import com.noshop.product_service.service.SubCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product/subcategories")
@RequiredArgsConstructor
public class SubCategoryController {

    private final SubCategoryService subCategoryService;


    @PostMapping
    public ResponseEntity<SubCategoryResponse> createSubCategory(
            @Valid @RequestBody CreateSubCategoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(subCategoryService.createSubCategory(request));
    }


    @GetMapping
    public ResponseEntity<List<SubCategoryResponse>> getAllSubCategories() {

        return ResponseEntity.ok(
                subCategoryService.getAllSubCategories()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<SubCategoryResponse> getSubCategoryById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                subCategoryService.getSubCategoryById(id)
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<SubCategoryResponse> updateSubCategory(
            @PathVariable Long id,
            @Valid @RequestBody CreateSubCategoryRequest request) {

        return ResponseEntity.ok(
                subCategoryService.updateSubCategory(id, request)
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubCategory(
            @PathVariable Long id) {

        subCategoryService.deleteSubCategory(id);

        return ResponseEntity.noContent().build();
    }
}