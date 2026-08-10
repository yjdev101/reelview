package com.reelview.controller;

import com.reelview.dto.request.RateRequest;
import com.reelview.entity.User;
import com.reelview.service.ReviewRatingService;
import com.reelview.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews/{reviewId}/ratings")
public class ReviewRatingController {
    private final ReviewRatingService reviewRatingService;
    private final UserService userService;

    public ReviewRatingController(ReviewRatingService reviewRatingService, UserService userService) {
        this.reviewRatingService = reviewRatingService;
        this.userService = userService;
    }

    @PostMapping
    public void rate(@PathVariable Long reviewId, @RequestBody RateRequest request, Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        reviewRatingService.rate(reviewId, user.getId(), request.getRating());
    }

    @GetMapping("/average")
    public Double getAverageRating(@PathVariable Long reviewId) {
        return reviewRatingService.getAverageRating(reviewId);
    }
}
