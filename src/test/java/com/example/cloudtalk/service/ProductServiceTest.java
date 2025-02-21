package com.example.cloudtalk.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.cloudtalk.model.Product;
import com.example.cloudtalk.model.ProductReviewSummary;
import com.example.cloudtalk.model.Review;
import com.example.cloudtalk.repository.ProductRepository;
import com.example.cloudtalk.repository.ReviewRepository;
import com.example.cloudtalk.repository.ProductReviewSummaryRepository;
import com.example.cloudtalk.service.ProductService;
import com.example.cloudtalk.service.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductReviewSummaryRepository productReviewSummaryRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Review review;
    private ProductReviewSummary productReviewSummary;

    @BeforeEach
    void setUp() {
        product = new Product(1L, "Test Product", "Description", BigDecimal.valueOf(99.99), null, null);
        review = new Review(1L, "John", "Doe", "Great product!", 5, product);
        productReviewSummary = new ProductReviewSummary(product, BigDecimal.valueOf(4.5), 10);
    }

    @Test
    void testGetAllProducts() {
        List<Product> products = Arrays.asList(product);
        when(productRepository.findAll()).thenReturn(products);
        when(redisCacheService.getReviews(1L)).thenReturn(Arrays.asList(review));
        when(redisCacheService.getProductReviewSummary(1L)).thenReturn(productReviewSummary);

        List<Product> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        assertEquals(1, result.get(0).getReviews().size());
    }

    @Test
    void testGetProductById_Found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(redisCacheService.getReviews(1L)).thenReturn(Arrays.asList(review));
        when(redisCacheService.getProductReviewSummary(1L)).thenReturn(productReviewSummary);

        Optional<Product> result = productService.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Product", result.get().getName());
        assertEquals(1, result.get().getReviews().size());
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void testCreateProduct() {
        when(productRepository.save(product)).thenReturn(product);

        Product createdProduct = productService.createProduct(product);

        assertNotNull(createdProduct);
        assertEquals("Test Product", createdProduct.getName());
    }

    @Test
    void testUpdateProduct_Success() {
        Product updatedProduct = new Product(1L, "Updated Name", "Updated Description", BigDecimal.valueOf(79.99), null, null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        Product result = productService.updateProduct(1L, updatedProduct);

        assertEquals("Updated Name", result.getName());
        assertEquals(BigDecimal.valueOf(79.99), result.getPrice());
    }

    @Test
    void testUpdateProduct_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.updateProduct(1L, product));
    }

    @Test
    void testDeleteProduct() {
        doNothing().when(productRepository).deleteById(1L);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository, times(1)).deleteById(1L);
    }
}