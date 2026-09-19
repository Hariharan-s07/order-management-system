package com.example.order_management_system.service;

import com.example.order_management_system.dto.ProductRequest;
import com.example.order_management_system.dto.ProductResponse;
import com.example.order_management_system.entity.Product;
import com.example.order_management_system.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(ProductRequest request) {

        Product product = new Product();

        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setAvailableQuantity(request.getAvailableQuantity());

        product.setActive(true);

        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        Product savedProduct = productRepository.save(product);

        return new ProductResponse(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getCategory(),
                savedProduct.getPrice(),
                savedProduct.getAvailableQuantity(),
                savedProduct.getActive(),
                savedProduct.getCreatedAt(),
                savedProduct.getUpdatedAt()
        );
    }
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found with id: " + id));

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getAvailableQuantity(),
                product.getActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
    public List<ProductResponse> getAllProducts() {

        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getCategory(),
                        product.getPrice(),
                        product.getAvailableQuantity(),
                        product.getActive(),
                        product.getCreatedAt(),
                        product.getUpdatedAt()
                ))
                .toList();
    }
    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found with id: " + id));

        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setAvailableQuantity(request.getAvailableQuantity());

        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);

        return new ProductResponse(
                updatedProduct.getId(),
                updatedProduct.getName(),
                updatedProduct.getCategory(),
                updatedProduct.getPrice(),
                updatedProduct.getAvailableQuantity(),
                updatedProduct.getActive(),
                updatedProduct.getCreatedAt(),
                updatedProduct.getUpdatedAt()
        );
    }
    public ProductResponse deactivateProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found with id: " + id));

        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);

        return new ProductResponse(
                updatedProduct.getId(),
                updatedProduct.getName(),
                updatedProduct.getCategory(),
                updatedProduct.getPrice(),
                updatedProduct.getAvailableQuantity(),
                updatedProduct.getActive(),
                updatedProduct.getCreatedAt(),
                updatedProduct.getUpdatedAt()
        );
    }
    public List<ProductResponse> searchProducts(String name) {

        List<Product> products =
                productRepository.findByNameContainingIgnoreCase(name);

        return products.stream()
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getCategory(),
                        product.getPrice(),
                        product.getAvailableQuantity(),
                        product.getActive(),
                        product.getCreatedAt(),
                        product.getUpdatedAt()
                ))
                .toList();
    }
}