package com.reelview.dto.response;

import com.reelview.entity.ContentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ContentResponse {

    private Long contentId;
    private String title;
    private ContentType contentType;
    private Integer releaseYear;
    private String description;
    private LocalDateTime createdAt;
    private List<String> genreNames;

}
