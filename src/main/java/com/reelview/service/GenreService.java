package com.reelview.service;

import com.reelview.entity.Genre;
import com.reelview.repository.GenreRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GenreService {
    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public Genre findOrCreate(String name) {
        Optional<Genre> existing = genreRepository.findByName(name);
        if (existing.isPresent()) {
            return existing.get();
        }
        Genre genre = new Genre();
        genre.setName(name);
        return genreRepository.save(genre);
    }
}
