package com.noshop.product_service.controller;

import com.noshop.product_service.dto.request.CreateSizeRequest;
import com.noshop.product_service.dto.response.SizeResponse;
import com.noshop.product_service.service.SizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product/sizes")
@RequiredArgsConstructor
public class SizeController {

    private final SizeService sizeService;

    @PostMapping
    public ResponseEntity<SizeResponse> createSize(
            @Valid @RequestBody CreateSizeRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(sizeService.createSize(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SizeResponse> getSizeById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                sizeService.getSizeById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<SizeResponse>> getAllSizes() {

        return ResponseEntity.ok(
                sizeService.getAllSizes()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SizeResponse> updateSize(
            @PathVariable Long id,
            @Valid @RequestBody CreateSizeRequest request) {

        return ResponseEntity.ok(
                sizeService.updateSize(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSize(
            @PathVariable Long id) {

        sizeService.deleteSize(id);
        return ResponseEntity.noContent().build();
    }
}