package com.example.demo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "overall_price", nullable = false)
    private Double overallPrice;

    @Column(name = "money_spent", nullable = false)
    private Double moneySpent;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @PrePersist
    @PreUpdate
    private void initialize() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (status == null) {
            status = OrderStatus.PACKING;
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }
        if (product != null && quantity != null) {
            this.overallPrice = product.getSoldPrice() * quantity;
            this.moneySpent = product.getPrice() * quantity;
        }
    }

    public enum OrderStatus {
        PACKING("Packing"),
        PACKING_COMPLETED("Packing Completed"),
        IN_TRANSIT("In Transit"),
        IN_LOCAL_WAREHOUSE("In Local Warehouse"),
        IN_DELIVERY("In Delivery"),
        DELIVERED("Delivered");

        private final String label;

        OrderStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum PaymentStatus {
        PENDING("Pending"),
        PAID("Paid"),
        PARTIAL("Partial"),
        REFUNDED("Refunded"),
        FAILED("Failed");

        private final String label;

        PaymentStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
