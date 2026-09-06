package com.noshop.product_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true,length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    private String logoUrl;

    private boolean active = true;

    @OneToMany(mappedBy = "brand")
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}