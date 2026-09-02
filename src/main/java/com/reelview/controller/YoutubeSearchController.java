package com.reelview.controller;

import com.reelview.client.youtube.YoutubeClient;
import com.reelview.client.youtube.YoutubeVideoCandidate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/youtube")
public class YoutubeSearchController {
    private final YoutubeClient youtubeClient;

    public YoutubeSearchController(YoutubeClient youtubeClient) {
        this.youtubeClient = youtubeClient;
    }

    @GetMapping("/search")
    public List<YoutubeVideoCandidate> searchVideos(@RequestParam String query, @RequestParam(defaultValue = "5") int maxResults) {
        return youtubeClient.searchVideos(query, maxResults);
    }
}
