package com.reelview.controller;

import com.reelview.client.tmdb.TmdbClient;
import com.reelview.client.tmdb.TmdbImportService;
import com.reelview.client.tmdb.TmdbMovieDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/tmdb")
public class TmdbTestController {
    private final TmdbClient tmdbClient;
    private final TmdbImportService tmdbImportService;

    public TmdbTestController(TmdbClient tmdbClient, TmdbImportService tmdbImportService) {
        this.tmdbClient = tmdbClient;
        this.tmdbImportService = tmdbImportService;
    }

    @GetMapping("/popular")
    public List<TmdbMovieDto> testPopular(@RequestParam(defaultValue = "1") int page) {
        return tmdbClient.getPopularMovies(page);
    }

    @PostMapping("/import")
    public String importMovies(@RequestParam(defaultValue = "1") int pages) {
        int count = tmdbImportService.importPopularMovies(pages);
        return count + "개 작품 등록 완료";
    }

}
