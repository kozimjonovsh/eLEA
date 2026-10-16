package com.example.demo.Controller;

import com.example.demo.Entity.Category;
import com.example.demo.Entity.Product;
import com.example.demo.Service.CategoryService;
import com.example.demo.Service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productService.search(keyword, pageable);
        } else if (categoryId != null) {
            products = productService.findByCategoryId(categoryId, pageable);
        } else {
            products = productService.findAll(pageable);
        }
        model.addAttribute("pageTitle", "Products");
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findAllList());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        return "products/list";
    }

    @GetMapping("/new")
    public String newProduct(Model model) {
        model.addAttribute("pageTitle", "New Product");
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAllList());
        return "products/form";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Product product = productService.findByIdWithCategory(id).orElse(null);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Product not found: " + id);
            return "redirect:/products";
        }
        model.addAttribute("pageTitle", product.getProductName());
        model.addAttribute("product", product);
        return "products/view";
    }

    @GetMapping("/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Product product = productService.findByIdWithCategory(id).orElse(null);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Product not found: " + id);
            return "redirect:/products";
        }
        model.addAttribute("pageTitle", "Edit Product");
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAllList());
        return "products/form";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam Long categoryId,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            product.setCategory(category);
            if (imageFile != null && !imageFile.isEmpty()) {
                String savedPath = storeImage(imageFile);
                deleteStoredImage(product.getProductPicture());
                product.setProductPicture(savedPath);
            }
            productService.save(product);
            redirectAttributes.addFlashAttribute("success", "Product saved successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Could not upload image: " + e.getMessage());
        }
        return "redirect:/products";
    }

    private String storeImage(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Please upload an image file");
        }
        String originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String cleanName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String filename = System.currentTimeMillis() + "_" + cleanName;
        Path uploadDir = Paths.get("uploads").toAbsolutePath();
        Files.createDirectories(uploadDir);
        Path target = uploadDir.resolve(filename).normalize();
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return "/uploads/" + filename;
    }

    private void deleteStoredImage(String picture) {
        if (picture != null && picture.startsWith("/uploads/")) {
            try {
                Path uploadDir = Paths.get("uploads").toAbsolutePath();
                Path file = uploadDir.resolve(picture.substring("/uploads/".length())).normalize();
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
            }
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete product: " + e.getMessage());
        }
        return "redirect:/products";
    }
}