package com.reelview.controller;

import com.reelview.dto.request.CreateContentRequest;
import com.reelview.dto.request.UpdateContentRequest;
import com.reelview.dto.response.ContentResponse;
import com.reelview.entity.Content;
import com.reelview.entity.Genre;
import com.reelview.service.ContentService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/contents")
public class ContentController {
    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
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

    @GetMapping
    public List<ContentResponse> getAllContents() {
        List<ContentResponse> response = new ArrayList<>();
        for (Content content : contentService.getAllContents()) {
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
