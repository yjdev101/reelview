package com.reelview.controller;

import com.reelview.dto.request.CreateContentRequest;
import com.reelview.dto.request.UpdateContentRequest;
import com.reelview.dto.response.ContentResponse;
import com.reelview.dto.response.ReviewResponse;
import com.reelview.entity.Content;
import com.reelview.entity.ContentType;
import com.reelview.entity.Genre;
import com.reelview.entity.Review;
import com.reelview.service.ContentService;
import com.reelview.service.ReviewService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/contents")
public class ContentController {
    private final ContentService contentService;
    private final ReviewService reviewService;

    public ContentController(ContentService contentService, ReviewService reviewService) {

        this.contentService = contentService;
        this.reviewService = reviewService;
    }

    @PostMapping
    public ContentResponse createContent(@RequestBody CreateContentRequest request) {
        Content content = contentService.createContent(request.getTitle(), request.getType(), request.getReleaseYear(), request.getDescription(), request.getGenreNames());
        return toResponse(content);
    }

    @GetMapping("/{id}")
    public ContentResponse getContent(@PathVariable Long id) {
        Content content = contentService.getContent(id);
        return toResponse(content);
    }

    @GetMapping("/{contentId}/reviews")
    public List<ReviewResponse> getContentReviews(@PathVariable Long contentId) {
        List<Review> reviews = reviewService.getReviewsByContent(contentId);
        List<ReviewResponse> reviewResponses = new ArrayList<>();
        for (Review review : reviews) {
            reviewResponses.add( new ReviewResponse(
                    review.getId(), review.getUser().getId(), contentId, review.getTitle(), review.getReviewType(), review.getVideoUrl(), review.getVideoFilePath(), review.getDescription(), review.getCommentSummary(), review.getCreatedAt(), review.getUpdatedAt()
            ));
        }
        return reviewResponses;
    }

    @GetMapping
    public List<ContentResponse> getAllContents(@RequestParam(required = false) String genre,
                                                @RequestParam(required = false) ContentType type) {

        List<Content> contents;
        if (genre != null) {
            contents = contentService.getContentsByGenres(genre);
        } else if (type != null) {
            contents = contentService.getContentsByType(type);
        } else {
            contents = contentService.getAllContents();
        }
        List<ContentResponse> response = new ArrayList<>();
        for (Content content : contents) {
            response.add(toResponse(content));
        }
        return response;
    }

    @PutMapping("/{id}")
    public ContentResponse updateContent(@PathVariable Long id, @RequestBody UpdateContentRequest request) {
        Content content = contentService.updateContent(id, request.getTitle(), request.getType(), request.getReleaseYear(), request.getDescription());
        return toResponse(content);
    }

    @DeleteMapping("/{id}")
    public void deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
    }

    private ContentResponse toResponse(Content content) {
        List<String> genreNames = new ArrayList<>();
        for (Genre genre : content.getGenres()) {
            genreNames.add(genre.getName());
        }
        return new ContentResponse(content.getId(), content.getTitle(), content.getType(), content.getReleaseYear(), content.getDescription(), content.getCreatedAt(), genreNames);
    }
}
