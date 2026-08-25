package com.reelview.controller;

import com.reelview.dto.response.GenreResponse;
import com.reelview.entity.Genre;
import com.reelview.service.GenreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/genres")
public class GenreController {
    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public List<GenreResponse> getAllGenres() {
        List<Genre> genres = genreService.getAllGenres();
        List<GenreResponse> response = new ArrayList<>();
        for (Genre genre : genres) {
            response.add(new GenreResponse(genre.getId(), genre.getName()));
        }
        return response;
    }
}
