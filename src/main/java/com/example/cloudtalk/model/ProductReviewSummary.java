package com.example.cloudtalk.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "product_review_summaries")
@NoArgsConstructor
@Data
public class ProductReviewSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;
    
    @Version
    private Long version;

    private BigDecimal averageReview;
    private int numberOfReviews;
    
    public ProductReviewSummary(Product product, BigDecimal averageReview, int numberOfReviews) {
        this.product = product;
        this.averageReview = averageReview;
        this.numberOfReviews = numberOfReviews;
    }

    public void updateSummary(BigDecimal newAverage, int totalReviews) {
        this.averageReview = newAverage;
        this.numberOfReviews = totalReviews;
    }

}
