package com.example.demo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_links", columnDefinition = "TEXT")
    private String productLinks;

    @Column(name = "product_colors", columnDefinition = "TEXT")
    private String productColors;

    @Column(name = "product_picture", columnDefinition = "TEXT")
    private String productPicture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "selling_types", columnDefinition = "TEXT")
    private String sellingTypes;

    @Column(name = "real_price", nullable = false)
    private Double realPrice;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "sold_price", nullable = false)
    private Double soldPrice;

    @PrePersist
    @PreUpdate
    private void calculatePrices() {
        if (realPrice != null) {
            this.price = Math.round(realPrice * 1.03 * 1.05 * 100.0) / 100.0;

            double baseSoldPrice;
            if (this.price <= 5000) {
                baseSoldPrice = this.price * 1.6;
            } else if (this.price <= 10000) {
                baseSoldPrice = this.price * 1.5;
            } else if (this.price <= 20000) {
                baseSoldPrice = this.price * 1.35;
            } else {
                baseSoldPrice = this.price * 1.25;
            }
            this.soldPrice = Math.round(baseSoldPrice / 1000.0) * 1000.0;
        }
    }
}
