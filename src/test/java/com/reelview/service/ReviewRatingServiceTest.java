package com.reelview.service;

import com.reelview.entity.ReviewRating;
import com.reelview.repository.ReviewRatingRepository;
import com.reelview.repository.ReviewRepository;
import com.reelview.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewRatingServiceTest {

    private ReviewRatingRepository reviewRatingRepository;
    private ReviewRatingService reviewRatingService;

    @BeforeEach
    void setUp() {
        reviewRatingRepository = mock(ReviewRatingRepository.class);
        ReviewRepository reviewRepository = mock(ReviewRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        reviewRatingService = new ReviewRatingService(reviewRatingRepository, reviewRepository, userRepository);
    }

    private ReviewRating rating(Long id, Integer value) {
        ReviewRating reviewRating = new ReviewRating();
        reviewRating.setId(id);
        reviewRating.setRating(value);
        return reviewRating;
    }

    @Test
    void updateRating_본인평가면_평점이바뀐다() {
        ReviewRating existing = rating(1L, 3);
        when(reviewRatingRepository.findByReviewIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));
        when(reviewRatingRepository.save(any(ReviewRating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewRating updated = reviewRatingService.updateRating(10L, 1L, 5);

        assertEquals(5, updated.getRating());
    }

    @Test
    void updateRating_평가한적없으면_NoSuchElementException() {
        when(reviewRatingRepository.findByReviewIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> reviewRatingService.updateRating(10L, 1L, 5));
    }

    @Test
    void deleteRating_본인평가면_삭제된다() {
        ReviewRating existing = rating(1L, 3);
        when(reviewRatingRepository.findByReviewIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));

        reviewRatingService.deleteRating(10L, 1L);

        verify(reviewRatingRepository).deleteById(1L);
    }

    @Test
    void deleteRating_평가한적없으면_NoSuchElementException() {
        when(reviewRatingRepository.findByReviewIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> reviewRatingService.deleteRating(10L, 1L));
    }
}
