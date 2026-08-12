package com.reelview.service;

import com.reelview.entity.Content;
import com.reelview.entity.ContentType;
import com.reelview.entity.Genre;
import com.reelview.repository.ContentRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ContentService {
    private final ContentRepository contentRepository;
    private final GenreService genreService;

    public ContentService(ContentRepository contentRepository, GenreService genreService) {
        this.contentRepository = contentRepository;
        this.genreService = genreService;
    }

    public Content createContent(String title, ContentType type, Integer releaseYear, String description, List<String> genreNames) {
        Content content = new Content();

        content.setTitle(title);
        content.setType(type);
        content.setReleaseYear(releaseYear);
        content.setDescription(description);
        content.setCreatedAt(LocalDateTime.now());

        Set<Genre> genres = new HashSet<>();
        for (String name : genreNames) {
            genres.add(genreService.findOrCreate(name));
        }
        content.setGenres(genres);

        return contentRepository.save(content);
    }

    public Content getContent(Long id) {

        return contentRepository.findById(id).orElseThrow();
    }

    public List<Content> getAllContents() {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return contentRepository.findAll(sort);
    }

    public List<Content> getContentsByGenres(String genreName) {
        return contentRepository.findByGenres_Name(genreName, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public List<Content> getContentsByType(ContentType type) {
        return contentRepository.findByType(type, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Content updateContent(Long id, String title, ContentType type, Integer releaseYear, String description) {
        Content content = contentRepository.findById(id).orElseThrow();
        content.setTitle(title);
        content.setType(type);
        content.setReleaseYear(releaseYear);
        content.setDescription(description);

        return contentRepository.save(content);
    }

    public void deleteContent(Long id) {
        contentRepository.deleteById(id);
    }
}