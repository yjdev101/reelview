package com.reelview.service;

import com.reelview.entity.Content;
import com.reelview.entity.Review;
import com.reelview.entity.ReviewType;
import com.reelview.entity.User;
import com.reelview.repository.ReviewRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review createReview(User user, Content content, String title,
                               ReviewType reviewType, String videoUrl,
                               String videoFilePath, String description) {

        Review review = new Review();

        review.setUser(user);
        review.setContent(content);
        review.setTitle(title);
        review.setReviewType(reviewType);
        review.setVideoUrl(videoUrl);
        review.setVideoFilePath(videoFilePath);
        review.setDescription(description);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public Review getReview(Long id) {
        return reviewRepository.findById(id).orElseThrow();
    }

    public Review updateReview(Long id, String title, String description, User requestUser) {
        Review review = reviewRepository.findById(id).orElseThrow();

        if (!review.getUser().getId().equals(requestUser.getId())) {
            throw new AccessDeniedException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        review.setTitle(title);
        review.setDescription(description);
        review.setUpdatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public void deleteReview(Long id, User requestUser) {
        Review review = reviewRepository.findById(id).orElseThrow();

        if (!review.getUser().getId().equals(requestUser.getId())) {
            throw new AccessDeniedException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        reviewRepository.deleteById(id);
    }
}
