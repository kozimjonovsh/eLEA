package com.example.demo.Service;

import com.example.demo.Entity.Order;
import com.example.demo.Repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public Page<Order> findAll(Specification<Order> spec, Pageable pageable) {
        return orderRepository.findAll(spec, pageable);
    }

    public List<Order> findAllList() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public List<Order> findByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> findByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> findByPaymentStatus(Order.PaymentStatus paymentStatus) {
        return orderRepository.findByPaymentStatus(paymentStatus);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> findByIdWithDetails(Long id) {
        return orderRepository.findByIdWithDetails(id);
    }

    public Order save(Order order) {
        if (order.getCustomer() == null || order.getCustomer().getId() == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        if (order.getProduct() == null || order.getProduct().getId() == null) {
            throw new IllegalArgumentException("Product is required");
        }
        if (order.getQuantity() == null || order.getQuantity() < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        if (order.getStatus() == null) {
            order.setStatus(Order.OrderStatus.PACKING);
        }
        if (order.getPaymentStatus() == null) {
            order.setPaymentStatus(Order.PaymentStatus.PENDING);
        }
        // Calculate prices
        order.setOverallPrice(order.getProduct().getSoldPrice() * order.getQuantity());
        order.setMoneySpent(order.getProduct().getPrice() * order.getQuantity());
        return orderRepository.save(order);
    }

    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }

    public long count() {
        return orderRepository.count();
    }

    public Double getTotalOverallPrice() {
        Double total = orderRepository.getTotalOverallPrice();
        return total != null ? total : 0.0;
    }

    public Double getTotalMoneySpent() {
        Double total = orderRepository.getTotalMoneySpent();
        return total != null ? total : 0.0;
    }

    public Long getTotalOrderCount() {
        Long count = orderRepository.getTotalOrderCount();
        return count != null ? count : 0L;
    }

    public Long getOrderCountSince(LocalDateTime since) {
        Long count = orderRepository.getOrderCountSince(since);
        return count != null ? count : 0L;
    }

    public Double getTotalProfit() {
        return getTotalOverallPrice() - getTotalMoneySpent();
    }
}