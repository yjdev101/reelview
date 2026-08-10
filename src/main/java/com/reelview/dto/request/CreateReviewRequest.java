package com.reelview.dto.request;

import com.reelview.entity.ReviewType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReviewRequest {

    private Long userId;
    private Long contentId;
    private String title;
    private ReviewType reviewType;
    private String videoUrl;
    private String videoFilePath;
    private String description;
}
