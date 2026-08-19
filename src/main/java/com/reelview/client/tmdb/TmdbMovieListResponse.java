package com.reelview.client.tmdb;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TmdbMovieListResponse {

    private List<TmdbMovieDto> results;
}
