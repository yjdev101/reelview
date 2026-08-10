package com.reelview.dto.request;

import com.reelview.entity.ContentType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateContentRequest {
   private String title;
   private ContentType type;
   private Integer releaseYear;
   private String description;

}
