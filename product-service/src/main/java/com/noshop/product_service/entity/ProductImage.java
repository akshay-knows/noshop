package com.noshop.product_service.entity;

import com.noshop.product_service.enums.ImageSource;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "product_images",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_image_order",
                        columnNames = {"product_id", "display_order"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String storageKey;

    @Column(length = 100)
    private String altText;

    /**
     * Image order starts from 1.
     * 1 = primary image
     * 2+ = secondary images
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ImageSource source = ImageSource.CATALOG;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}