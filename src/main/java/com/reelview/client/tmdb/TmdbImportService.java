package com.reelview.client.tmdb;

import com.reelview.entity.ContentType;
import com.reelview.service.ContentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TmdbImportService {
    private final TmdbClient tmdbClient;
    private final ContentService contentService;

    public TmdbImportService(TmdbClient tmdbClient, ContentService contentService) {
        this.tmdbClient = tmdbClient;
        this.contentService = contentService;
    }

    public TmdbImportResult importPopularMovies(int pages) {
        int imported = 0;
        int skipped = 0;
        for (int page = 1; page <= pages; page++) {
            List<TmdbMovieDto> movies = tmdbClient.getPopularMovies(page);
            for (TmdbMovieDto movie : movies) {
                Integer releaseYear = Integer.parseInt(movie.getReleaseDate().substring(0, 4));

                if (contentService.existsByTmdbId(movie.getId())) {
                    skipped++;
                    continue;
                }

                List<String> genreNames = TmdbGenreMapper.toGenreNames(movie.getGenreIds());
                contentService.createContent(movie.getId(), movie.getTitle(), ContentType.MOVIE, releaseYear, movie.getOverview(), genreNames);
                imported++;
            }
        }
        return new TmdbImportResult(imported, skipped);
    }
}
