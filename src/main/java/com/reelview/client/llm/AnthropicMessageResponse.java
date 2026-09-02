package com.reelview.client.llm;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AnthropicMessageResponse {

    private List<ContentBlock> content;

    @Getter
    @Setter
    public static class ContentBlock {
        private String type;
        private String text;
    }
}
