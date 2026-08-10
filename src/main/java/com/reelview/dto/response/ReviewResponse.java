package com.reelview.dto.response;

import com.reelview.entity.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReviewResponse {

    private Long reviewId;
    private Long userId;
    private Long contentId;
    private String title;
    private ReviewType reviewType;
    private String videoUrl;
    private String videoFilePath;
    private String description;
    private String commentSummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
