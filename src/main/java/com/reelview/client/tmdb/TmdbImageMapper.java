package com.reelview.client.tmdb;

public class TmdbImageMapper {

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    public static String toPosterUrl(String posterPath) {
        if (posterPath == null) return null;
        return IMAGE_BASE_URL + posterPath;
    }
}
