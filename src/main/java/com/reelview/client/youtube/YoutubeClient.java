package com.reelview.client.youtube;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class YoutubeClient {

    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile("(?:v=|youtu\\.be/)([a-zA-Z0-9_-]{11})");

    private final String apiKey;
    private final RestClient restClient;

    public YoutubeClient(@Value("${youtube.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.create("https://www.googleapis.com/youtube/v3");
    }

    public String extractVideoId(String videoUrl) {
        Matcher matcher = VIDEO_ID_PATTERN.matcher(videoUrl);
        if (!matcher.find()) {
            throw new IllegalArgumentException("유효한 유튜브 URL이 아닙니다.");
        }
        return matcher.group(1);
    }

    public List<String> getTopComments(String videoId, int maxResults) {
        YoutubeCommentThreadListResponse response;
        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/commentThreads")
                            .queryParam("part", "snippet")
                            .queryParam("videoId", videoId)
                            .queryParam("maxResults", maxResults)
                            .queryParam("order", "relevance")
                            .queryParam("textFormat", "plainText")
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(YoutubeCommentThreadListResponse.class);
        } catch (HttpClientErrorException e) {
            throw new IllegalArgumentException("댓글을 가져올 수 없는 영상입니다 (댓글 비활성화 또는 잘못된 영상 URL).");
        }

        if (response == null || response.getItems() == null) {
            return List.of();
        }

        return response.getItems().stream()
                .map(item -> item.getSnippet().getTopLevelComment().getSnippet().getTextDisplay())
                .collect(Collectors.toList());
    }

    public List<YoutubeVideoCandidate> searchVideos(String query, int maxResults) {
        YoutubeSearchListResponse response;
        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("part", "snippet")
                            .queryParam("q", query)
                            .queryParam("type", "video")
                            .queryParam("maxResults", maxResults)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(YoutubeSearchListResponse.class);
        } catch (HttpClientErrorException e) {
            throw new IllegalArgumentException("유튜브 검색에 실패했습니다.");
        }

        if (response == null || response.getItems() == null) {
            return List.of();
        }

        return response.getItems().stream()
                .map(item -> new YoutubeVideoCandidate(
                        item.getId().getVideoId(),
                        item.getSnippet().getTitle(),
                        item.getSnippet().getChannelTitle(),
                        "https://www.youtube.com/watch?v=" + item.getId().getVideoId()))
                .collect(Collectors.toList());
    }
}
