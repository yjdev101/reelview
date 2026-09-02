package com.reelview.client.youtube;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class YoutubeSearchListResponse {

    private List<Item> items;

    @Getter
    @Setter
    public static class Item {
        private Id id;
        private Snippet snippet;
    }

    @Getter
    @Setter
    public static class Id {
        private String videoId;
    }

    @Getter
    @Setter
    public static class Snippet {
        private String title;
        private String channelTitle;
    }
}
