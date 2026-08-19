package com.reelview.client.tmdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class TmdbClient {
    private final String apiKey;
    private final RestClient restClient;

    public TmdbClient(@Value("${tmdb.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.create("https://api.themoviedb.org/3");
    }

    public List<TmdbMovieDto> getPopularMovies(int page) {
        TmdbMovieListResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/popular")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(TmdbMovieListResponse.class);

        return response.getResults();
    }
}
