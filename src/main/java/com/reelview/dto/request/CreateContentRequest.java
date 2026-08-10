package com.reelview.dto.request;

import com.reelview.entity.ContentType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateContentRequest {

    private String title;
    private ContentType type;
    private Integer releaseYear;
    private String description;
    private List<String> genreNames;

}