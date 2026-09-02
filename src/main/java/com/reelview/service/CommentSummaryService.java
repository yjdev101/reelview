package com.reelview.service;

import com.reelview.client.llm.AnthropicClient;
import com.reelview.client.youtube.YoutubeClient;
import com.reelview.entity.Review;
import com.reelview.entity.ReviewType;
import com.reelview.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentSummaryService {

    private final ReviewRepository reviewRepository;
    private final YoutubeClient youtubeClient;
    private final AnthropicClient anthropicClient;

    public CommentSummaryService(ReviewRepository reviewRepository, YoutubeClient youtubeClient, AnthropicClient anthropicClient) {
        this.reviewRepository = reviewRepository;
        this.youtubeClient = youtubeClient;
        this.anthropicClient = anthropicClient;
    }

    public String getOrGenerateSummary(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow();

        if (review.getReviewType() != ReviewType.URL) {
            throw new IllegalArgumentException("URL 타입 리뷰만 댓글 요약을 지원합니다.");
        }

        if (review.getCommentSummary() != null) {
            return review.getCommentSummary();
        }

        String videoId = youtubeClient.extractVideoId(review.getVideoUrl());
        List<String> comments = youtubeClient.getTopComments(videoId, 20);

        if (comments.isEmpty()) {
            throw new IllegalArgumentException("요약할 댓글이 없습니다.");
        }

        String summary = anthropicClient.summarize(comments);
        review.setCommentSummary(summary);
        reviewRepository.save(review);

        return summary;
    }
}
