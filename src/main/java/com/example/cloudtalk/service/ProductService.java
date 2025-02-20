package com.example.cloudtalk.service;

import com.example.cloudtalk.model.Product;
import com.example.cloudtalk.model.ProductReviewSummary;
import com.example.cloudtalk.model.Review;
import com.example.cloudtalk.repository.ProductRepository;
import com.example.cloudtalk.repository.ProductReviewSummaryRepository;
import com.example.cloudtalk.repository.ReviewRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisCacheService redisCacheService;
    private final ReviewRepository reviewRepository;
    private final ProductReviewSummaryRepository productReviewSummaryRepository;   

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id).map(product -> {
            product.setReviews(getReviews(id));
            product.setProductReviewSummary(getProductReviewSumary(id));
            return product;
        });
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
    
    public List<Review> getReviews(Long productId) {

        log.info("USING CACHING");
        List<Review> cachedReviews = redisCacheService.getReviews(productId);
        
        if (cachedReviews != null) {
            log.info("USING CACHED VALUE for REVIEWS");
            return cachedReviews;
        }

        List<Review> reviews = reviewRepository.findByProductId(productId);
        redisCacheService.saveReviews(productId, reviews); 
        return reviews;
    }
    
    public ProductReviewSummary getProductReviewSumary(Long productId) {

        log.info("USING CACHING for PRS");
        ProductReviewSummary cachedProductReviewSummary = redisCacheService.getProductReviewSummary(productId);
        
        if (cachedProductReviewSummary != null) {
            log.info("USING CACHED VALUE for PRS");
            return cachedProductReviewSummary;
        }

        Optional<ProductReviewSummary> productReviewSummary = productReviewSummaryRepository.findByProductId(productId);
        if (productReviewSummary.isPresent()) {
            redisCacheService.saveProductReviewSummary(productId, productReviewSummary.get());
            return productReviewSummary.get();
        }
        else {
            clearCache(productId);
        }
        return null;
    }
    
    public void clearCache(Long productId) {
        redisCacheService.deleteReviews(productId);
        redisCacheService.deleteProductReviewSummary(productId);
    }
}
