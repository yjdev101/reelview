package com.reelview.service;

import com.reelview.entity.Review;
import com.reelview.entity.ReviewRating;
import com.reelview.entity.User;
import com.reelview.repository.ReviewRatingRepository;
import com.reelview.repository.ReviewRepository;
import com.reelview.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReviewRatingService {
    private final ReviewRatingRepository reviewRatingRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewRatingService(ReviewRatingRepository reviewRatingRepository, ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRatingRepository = reviewRatingRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    public ReviewRating rate(Long reviewId, Long userId, Integer rating) {
        Optional<ReviewRating> existing = reviewRatingRepository.findByReviewIdAndUserId(reviewId, userId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("이미 평가한 리뷰입니다.");
        }

        Review review = reviewRepository.findById(reviewId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        ReviewRating reviewRating = new ReviewRating();
        reviewRating.setReview(review);
        reviewRating.setUser(user);
        reviewRating.setRating(rating);
        reviewRating.setCreatedAt(LocalDateTime.now());

        return reviewRatingRepository.save(reviewRating);
    }

    public Double getAverageRating(Long reviewId) {
        return reviewRatingRepository.findAverageRatingByReviewId(reviewId);
    }
}
