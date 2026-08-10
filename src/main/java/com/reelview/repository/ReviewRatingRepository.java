package com.reelview.repository;

import com.reelview.entity.ReviewRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRatingRepository extends JpaRepository<ReviewRating, Long> {

    Optional<ReviewRating> findByReviewIdAndUserId(Long reviewId, Long userId);

    @Query("SELECT AVG(rr.rating) FROM ReviewRating rr WHERE rr.review.id = :reviewId")
    Double findAverageRatingByReviewId(@Param("reviewId") Long reviewId);
}
