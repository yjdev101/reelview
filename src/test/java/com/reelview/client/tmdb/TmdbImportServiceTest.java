package com.reelview.client.tmdb;

import com.reelview.entity.Content;
import com.reelview.entity.ContentType;
import com.reelview.service.ContentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TmdbImportServiceTest {

    private TmdbClient tmdbClient;
    private ContentService contentService;
    private TmdbImportService tmdbImportService;

    @BeforeEach
    void setUp() {
        tmdbClient = mock(TmdbClient.class);
        contentService = mock(ContentService.class);
        tmdbImportService = new TmdbImportService(tmdbClient, contentService);
    }

    private TmdbMovieDto movie(Long id, String title, String releaseDate) {
        TmdbMovieDto dto = new TmdbMovieDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setOverview("설명");
        dto.setReleaseDate(releaseDate);
        dto.setPosterPath("/poster.jpg");
        dto.setGenreIds(List.of(28));
        return dto;
    }

    @Test
    void importPopularMovies_이미등록됐고포스터도있으면_건너뛰기만한다() {
        Content existing = new Content();
        existing.setPosterUrl("https://image.tmdb.org/t/p/w500/poster.jpg");

        when(tmdbClient.getPopularMovies(1)).thenReturn(List.of(movie(438631L, "듄", "2021-09-15")));
        when(contentService.findByTmdbId(438631L)).thenReturn(Optional.of(existing));

        TmdbImportResult result = tmdbImportService.importPopularMovies(1);

        assertEquals(0, result.imported());
        assertEquals(1, result.skipped());
        verify(contentService, never()).createContent(anyLong(), anyString(), eq(ContentType.MOVIE), eq(2021), anyString(), anyString(), anyList());
        verify(contentService, never()).updatePoster(any(), anyString());
    }

    @Test
    void importPopularMovies_이미등록됐지만포스터가없으면_포스터만채운다() {
        Content existing = new Content();
        existing.setPosterUrl(null);

        when(tmdbClient.getPopularMovies(1)).thenReturn(List.of(movie(438631L, "듄", "2021-09-15")));
        when(contentService.findByTmdbId(438631L)).thenReturn(Optional.of(existing));

        TmdbImportResult result = tmdbImportService.importPopularMovies(1);

        assertEquals(0, result.imported());
        assertEquals(1, result.skipped());
        verify(contentService).updatePoster(existing, "https://image.tmdb.org/t/p/w500/poster.jpg");
        verify(contentService, never()).createContent(anyLong(), anyString(), eq(ContentType.MOVIE), eq(2021), anyString(), anyString(), anyList());
    }

    @Test
    void importPopularMovies_신규작품이면_등록한다() {
        when(tmdbClient.getPopularMovies(1)).thenReturn(List.of(movie(438631L, "듄", "2021-09-15")));
        when(contentService.findByTmdbId(438631L)).thenReturn(Optional.empty());

        TmdbImportResult result = tmdbImportService.importPopularMovies(1);

        assertEquals(1, result.imported());
        assertEquals(0, result.skipped());
        verify(contentService).createContent(eq(438631L), eq("듄"), eq(ContentType.MOVIE), eq(2021), eq("https://image.tmdb.org/t/p/w500/poster.jpg"), anyString(), anyList());
    }
}
