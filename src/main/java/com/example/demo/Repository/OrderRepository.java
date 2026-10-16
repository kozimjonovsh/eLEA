package com.example.demo.Repository;

import com.example.demo.Entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    @Override
    @EntityGraph(attributePaths = {"customer", "product", "product.category"})
    Page<Order> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"customer", "product", "product.category"})
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "product"})
    List<Order> findByCustomerId(Long customerId);

    @EntityGraph(attributePaths = {"customer", "product", "product.category"})
    List<Order> findAllByOrderByOrderDateDesc();

    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findByStatus(@Param("status") Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.paymentStatus = :paymentStatus")
    List<Order> findByPaymentStatus(@Param("paymentStatus") Order.PaymentStatus paymentStatus);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.product p LEFT JOIN FETCH p.category WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT SUM(o.overallPrice) FROM Order o")
    Double getTotalOverallPrice();

    @Query("SELECT SUM(o.moneySpent) FROM Order o")
    Double getTotalMoneySpent();

    @Query("SELECT COUNT(o) FROM Order o")
    Long getTotalOrderCount();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :since")
    Long getOrderCountSince(@Param("since") LocalDateTime since);
}
