package com.reelview.service;

import com.reelview.entity.Genre;
import com.reelview.repository.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GenreServiceTest {

    private GenreRepository genreRepository;
    private GenreService genreService;

    @BeforeEach
    void setUp() {
        genreRepository = mock(GenreRepository.class);
        genreService = new GenreService(genreRepository);
    }

    @Test
    void getAllGenres_등록된장르전체를반환한다() {
        Genre action = new Genre();
        action.setId(1L);
        action.setName("액션");
        Genre drama = new Genre();
        drama.setId(2L);
        drama.setName("드라마");
        when(genreRepository.findAll()).thenReturn(List.of(action, drama));

        List<Genre> genres = genreService.getAllGenres();

        assertEquals(2, genres.size());
    }
}
