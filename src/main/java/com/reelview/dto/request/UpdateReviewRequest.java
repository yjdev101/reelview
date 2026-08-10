package com.reelview.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateReviewRequest {

    @NotBlank
    private String title;

    private String description;
}
