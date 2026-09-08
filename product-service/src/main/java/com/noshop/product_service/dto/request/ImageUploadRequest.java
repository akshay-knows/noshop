package com.noshop.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ImageUploadRequest {

    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must not exceed 255 characters")
    private String fileName;

    @NotBlank(message = "Content type is required")
    @Pattern(
            regexp = "image/(jpeg|jpg|png|webp)",
            message = "Only JPEG, PNG and WebP images are allowed"
    )
    private String contentType;

    @Size(max = 100, message = "Alt text must not exceed 100 characters")
    private String altText;
}