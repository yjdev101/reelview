package com.reelview.service;

import com.reelview.entity.Review;
import com.reelview.entity.User;
import com.reelview.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewServiceTest {

    private ReviewRepository reviewRepository;
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewRepository = mock(ReviewRepository.class);
        reviewService = new ReviewService(reviewRepository);
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Review reviewOwnedBy(User owner) {
        Review review = new Review();
        review.setId(1L);
        review.setUser(owner);
        review.setTitle("원래 제목");
        review.setDescription("원래 설명");
        return review;
    }

    @Test
    void updateReview_본인리뷰면_수정된다() {
        User owner = user(1L);
        Review review = reviewOwnedBy(owner);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Review updated = reviewService.updateReview(1L, "새 제목", "새 설명", owner);

        assertEquals("새 제목", updated.getTitle());
        assertEquals("새 설명", updated.getDescription());
    }

    @Test
    void updateReview_다른사용자면_AccessDeniedException() {
        User owner = user(1L);
        User stranger = user(2L);
        Review review = reviewOwnedBy(owner);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(AccessDeniedException.class,
                () -> reviewService.updateReview(1L, "새 제목", "새 설명", stranger));
    }

    @Test
    void deleteReview_본인리뷰면_삭제된다() {
        User owner = user(1L);
        Review review = reviewOwnedBy(owner);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        reviewService.deleteReview(1L, owner);

        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void deleteReview_다른사용자면_AccessDeniedException() {
        User owner = user(1L);
        User stranger = user(2L);
        Review review = reviewOwnedBy(owner);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(AccessDeniedException.class,
                () -> reviewService.deleteReview(1L, stranger));

        verify(reviewRepository, never()).deleteById(any());
    }
}
