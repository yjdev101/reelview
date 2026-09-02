package com.reelview.client.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AnthropicMessageRequest {

    private String model;

    @JsonProperty("max_tokens")
    private int maxTokens;

    private List<Message> messages;

    public AnthropicMessageRequest(String model, int maxTokens, List<Message> messages) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.messages = messages;
    }

    @Getter
    @Setter
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
