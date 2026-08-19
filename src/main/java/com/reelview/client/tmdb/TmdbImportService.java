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

    public int importPopularMovies(int pages) {
        int count = 0;
        for (int page = 1; page <= pages; page++) {
            List<TmdbMovieDto> movies = tmdbClient.getPopularMovies(page);
            for (TmdbMovieDto movie : movies) {
                Integer releaseYear = Integer.parseInt(movie.getReleaseDate().substring(0, 4));
                List<String> genreNames = TmdbGenreMapper.toGenreNames(movie.getGenreIds());

                contentService.createContent(movie.getTitle(), ContentType.MOVIE, releaseYear, movie.getOverview(), genreNames);
                count++;
            }
        }
        return count;
    }
}
