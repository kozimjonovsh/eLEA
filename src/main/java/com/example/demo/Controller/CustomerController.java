package com.example.demo.Controller;

import com.example.demo.Entity.Customer;
import com.example.demo.Service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Customer> customers = customerService.search(keyword, pageable);
        model.addAttribute("pageTitle", "Customers");
        model.addAttribute("customers", customers);
        model.addAttribute("keyword", keyword);
        return "customers/list";
    }

    @GetMapping("/new")
    public String newCustomer(Model model) {
        model.addAttribute("pageTitle", "New Customer");
        model.addAttribute("customer", new Customer());
        model.addAttribute("sizes", Customer.Size.values());
        return "customers/form";
    }

    @GetMapping("/{id}")
    public String viewCustomer(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Customer customer = customerService.findByIdWithOrders(id).orElse(null);
        if (customer == null) {
            redirectAttributes.addFlashAttribute("error", "Customer not found: " + id);
            return "redirect:/customers";
        }
        model.addAttribute("pageTitle", customer.getName());
        model.addAttribute("customer", customer);
        return "customers/view";
    }

    @GetMapping("/{id}/edit")
    public String editCustomer(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Customer customer = customerService.findById(id).orElse(null);
        if (customer == null) {
            redirectAttributes.addFlashAttribute("error", "Customer not found: " + id);
            return "redirect:/customers";
        }
        model.addAttribute("pageTitle", "Edit Customer");
        model.addAttribute("customer", customer);
        model.addAttribute("sizes", Customer.Size.values());
        return "customers/form";
    }

    @PostMapping("/save")
    public String saveCustomer(@ModelAttribute Customer customer, RedirectAttributes redirectAttributes) {
        try {
            customerService.save(customer);
            redirectAttributes.addFlashAttribute("success", "Customer saved successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customers";
    }

    @PostMapping("/{id}/delete")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Customer deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete customer: " + e.getMessage());
        }
        return "redirect:/customers";
    }
}
