package com.reelview.client.youtube;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class YoutubeCommentThreadListResponse {

    private List<Item> items;

    @Getter
    @Setter
    public static class Item {
        private Snippet snippet;
    }

    @Getter
    @Setter
    public static class Snippet {
        private TopLevelComment topLevelComment;
    }

    @Getter
    @Setter
    public static class TopLevelComment {
        private CommentSnippet snippet;
    }

    @Getter
    @Setter
    public static class CommentSnippet {
        private String textDisplay;
    }
}
