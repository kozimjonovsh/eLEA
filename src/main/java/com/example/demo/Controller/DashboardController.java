package com.example.demo.Controller;

import com.example.demo.Entity.Order;
import com.example.demo.Service.CustomerService;
import com.example.demo.Service.OrderService;
import com.example.demo.Service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/")
public class DashboardController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final ProductService productService;

    public DashboardController(OrderService orderService,
                               CustomerService customerService,
                               ProductService productService) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.productService = productService;
    }

    @GetMapping
    public String dashboard(Model model) {
        long totalCustomers = customerService.count();
        long totalProducts = productService.count();
        long totalOrders = orderService.getTotalOrderCount();
        double totalMoneyGot = orderService.getTotalOverallPrice();
        double totalMoneySpent = orderService.getTotalMoneySpent();
        double totalProfit = totalMoneyGot - totalMoneySpent;
        long ordersThisMonth = orderService.getOrderCountSince(LocalDateTime.now().minusDays(30));
        long pendingOrders = orderService.findByStatus(Order.OrderStatus.PACKING).size() +
                orderService.findByStatus(Order.OrderStatus.PACKING_COMPLETED).size();
        long inTransitOrders = orderService.findByStatus(Order.OrderStatus.IN_TRANSIT).size() +
                orderService.findByStatus(Order.OrderStatus.IN_LOCAL_WAREHOUSE).size();
        long inDeliveryOrders = orderService.findByStatus(Order.OrderStatus.IN_DELIVERY).size();
        long deliveredOrders = orderService.findByStatus(Order.OrderStatus.DELIVERED).size();

        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalMoneyGot", totalMoneyGot);
        model.addAttribute("totalMoneySpent", totalMoneySpent);
        model.addAttribute("totalProfit", totalProfit);
        model.addAttribute("ordersThisMonth", ordersThisMonth);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("inTransitOrders", inTransitOrders);
        model.addAttribute("inDeliveryOrders", inDeliveryOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);

        // Recent orders
        model.addAttribute("recentOrders", orderService.findAllList().stream().limit(5).toList());

        return "dashboard";
    }
}