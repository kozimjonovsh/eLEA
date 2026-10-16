package com.example.demo.Controller;

import com.example.demo.Entity.Customer;
import com.example.demo.Entity.Order;
import com.example.demo.Entity.Product;
import com.example.demo.Service.CustomerService;
import com.example.demo.Service.OrderService;
import com.example.demo.Service.ProductService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final ProductService productService;

    public OrderController(OrderService orderService,
                           CustomerService customerService,
                           ProductService productService) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.productService = productService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String paymentStatus,
                       @RequestParam(required = false) Long customerId,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        Order.OrderStatus statusEnum = parseStatus(status);
        Order.PaymentStatus paymentEnum = parsePaymentStatus(paymentStatus);
        Specification<Order> spec = buildSpec(keyword, statusEnum, paymentEnum, customerId);
        Page<Order> orders = orderService.findAll(spec, pageable);
        model.addAttribute("pageTitle", "Orders");
        model.addAttribute("orders", orders);
        model.addAttribute("customers", customerService.findAllList());
        model.addAttribute("products", productService.findAllList());
        model.addAttribute("statuses", Order.OrderStatus.values());
        model.addAttribute("paymentStatuses", Order.PaymentStatus.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPaymentStatus", paymentStatus);
        model.addAttribute("selectedCustomerId", customerId);
        return "orders/list";
    }

    private Order.OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return Order.OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Order.PaymentStatus parsePaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.isBlank()) return null;
        try {
            return Order.PaymentStatus.valueOf(paymentStatus);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Specification<Order> buildSpec(String keyword,
                                           Order.OrderStatus status,
                                           Order.PaymentStatus paymentStatus,
                                           Long customerId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("customer").get("name")), like),
                        cb.like(cb.lower(root.get("product").get("productName")), like)
                ));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (paymentStatus != null) {
                predicates.add(cb.equal(root.get("paymentStatus"), paymentStatus));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @GetMapping("/new")
    public String newOrder(@RequestParam(required = false) Long customerId, Model model) {
        Order order = new Order();
        if (customerId != null) {
            customerService.findById(customerId).ifPresent(order::setCustomer);
        }
        model.addAttribute("pageTitle", "New Order");
        model.addAttribute("order", order);
        model.addAttribute("customers", customerService.findAllList());
        model.addAttribute("products", productService.findAllList());
        model.addAttribute("statuses", Order.OrderStatus.values());
        model.addAttribute("paymentStatuses", Order.PaymentStatus.values());
        return "orders/form";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Order order = orderService.findByIdWithDetails(id).orElse(null);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Order not found: " + id);
            return "redirect:/orders";
        }
        model.addAttribute("pageTitle", "Order #" + id);
        model.addAttribute("order", order);
        return "orders/view";
    }

    @GetMapping("/{id}/edit")
    public String editOrder(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Order order = orderService.findByIdWithDetails(id).orElse(null);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Order not found: " + id);
            return "redirect:/orders";
        }
        model.addAttribute("pageTitle", "Edit Order #" + id);
        model.addAttribute("order", order);
        model.addAttribute("customers", customerService.findAllList());
        model.addAttribute("products", productService.findAllList());
        model.addAttribute("statuses", Order.OrderStatus.values());
        model.addAttribute("paymentStatuses", Order.PaymentStatus.values());
        return "orders/form";
    }

    @PostMapping("/save")
    public String saveOrder(@ModelAttribute Order order,
                            @RequestParam Long customerId,
                            @RequestParam Long productId,
                            RedirectAttributes redirectAttributes) {
        try {
            Customer customer = customerService.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            Product product = productService.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            order.setCustomer(customer);
            order.setProduct(product);
            orderService.save(order);
            redirectAttributes.addFlashAttribute("success", "Order saved successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/{id}/delete")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Order deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete order: " + e.getMessage());
        }
        return "redirect:/orders";
    }

    @GetMapping("/api/product-price/{productId}")
    @ResponseBody
    public Product getProductPrice(@PathVariable Long productId) {
        return productService.findByIdWithCategory(productId).orElse(null);
    }
}