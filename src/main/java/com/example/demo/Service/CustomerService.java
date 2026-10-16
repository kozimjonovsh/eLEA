package com.example.demo.Service;

import com.example.demo.Entity.Customer;
import com.example.demo.Repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Page<Customer> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    public Page<Customer> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return customerRepository.findAll(pageable);
        }
        return customerRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
    }

    public List<Customer> findAllList() {
        return customerRepository.findAllByOrderByIdDesc();
    }

    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> findByIdWithOrders(Long id) {
        return customerRepository.findByIdWithOrders(id);
    }

    public Customer save(Customer customer) {
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (customer.getShoeSize() != null && (customer.getShoeSize() < 30 || customer.getShoeSize() > 50)) {
            throw new IllegalArgumentException("Shoe size must be between 30 and 50");
        }
        customer.setName(customer.getName().trim());
        if (customer.getSocialMediaAccounts() != null) {
            customer.setSocialMediaAccounts(customer.getSocialMediaAccounts().trim());
        }
        return customerRepository.save(customer);
    }

    public void deleteById(Long id) {
        customerRepository.deleteById(id);
    }

    public long count() {
        return customerRepository.count();
    }
}