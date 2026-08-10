package com.reelview.controller;

import com.reelview.dto.request.CreateReviewRequest;
import com.reelview.dto.request.UpdateReviewRequest;
import com.reelview.dto.response.ReviewResponse;
import com.reelview.entity.Content;
import com.reelview.entity.Review;
import com.reelview.entity.User;
import com.reelview.service.ContentService;
import com.reelview.service.ReviewService;
import com.reelview.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final UserService userService;
    private final ContentService contentService;

    public ReviewController(ReviewService reviewService, UserService userService, ContentService contentService) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.contentService = contentService;
    }

    @PostMapping
    public ReviewResponse createReview(@RequestBody CreateReviewRequest request) {

        User user = userService.getUser(request.getUserId());
        Content content = contentService.getContent(request.getContentId());
        Review review = reviewService.createReview(user, content, request.getTitle(), request.getReviewType(), request.getVideoUrl(), request.getVideoFilePath(), request.getDescription());

        return new ReviewResponse(review.getId(), user.getId(), content.getId(), review.getTitle(), review.getReviewType(), review.getVideoUrl(), review.getVideoFilePath(), review.getDescription(), review.getCommentSummary(), review.getCreatedAt(), review.getUpdatedAt());
    }

    @GetMapping("/{id}")
    public ReviewResponse getReview(@PathVariable Long id) {

        Review review = reviewService.getReview(id);
        User user = review.getUser();
        Content content = review.getContent();
        return new ReviewResponse(review.getId(), user.getId(), content.getId(), review.getTitle(), review.getReviewType(), review.getVideoUrl(), review.getVideoFilePath(), review.getDescription(), review.getCommentSummary(), review.getCreatedAt(), review.getUpdatedAt());
    }

    @PutMapping("/{id}")
    public ReviewResponse updateReview(@PathVariable Long id, @Valid @RequestBody UpdateReviewRequest request) {

        Review review = reviewService.updateReview(id, request.getTitle(), request.getDescription());
        User user = review.getUser();
        Content content = review.getContent();
        return new ReviewResponse(review.getId(), user.getId(), content.getId(), review.getTitle(), review.getReviewType(), review.getVideoUrl(), review.getVideoFilePath(), review.getDescription(), review.getCommentSummary(), review.getCreatedAt(), review.getUpdatedAt());
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }
}
