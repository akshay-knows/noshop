package com.noshop.product_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sizes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Size {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    private Integer displayOrder;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    @OneToMany(mappedBy = "size")
    private List<ProductVariant> variants = new ArrayList<>();
}