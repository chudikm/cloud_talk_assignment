package com.example.cloudtalk.service;

import com.example.cloudtalk.model.Product;
import com.example.cloudtalk.repository.ProductRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(updatedProduct.getName());
                    existingProduct.setDescription(updatedProduct.getDescription());
                    existingProduct.setPrice(updatedProduct.getPrice());
                    return productRepository.save(existingProduct);
                }).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
