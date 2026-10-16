package com.example.demo.Service;

import com.example.demo.Entity.Product;
import com.example.demo.Repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.findByProductNameContainingIgnoreCase(keyword.trim(), pageable);
    }

    public List<Product> findAllList() {
        return productRepository.findAllByOrderByIdDesc();
    }

    public List<Product> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public Page<Product> findByCategoryId(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    public long countByCategoryId(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> findByIdWithCategory(Long id) {
        return productRepository.findByIdWithCategory(id);
    }

    public Product save(Product product) {
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getRealPrice() == null || product.getRealPrice() < 0) {
            throw new IllegalArgumentException("Real price must be a positive number");
        }
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new IllegalArgumentException("Category is required");
        }
        product.setProductName(product.getProductName().trim());
        if (product.getProductLinks() != null) {
            product.setProductLinks(product.getProductLinks().trim());
        }
        if (product.getProductColors() != null) {
            product.setProductColors(product.getProductColors().trim());
        }
        if (product.getProductPicture() != null) {
            product.setProductPicture(product.getProductPicture().trim());
        }
        if (product.getSellingTypes() != null) {
            product.setSellingTypes(product.getSellingTypes().trim());
        }
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public long count() {
        return productRepository.count();
    }
}