package com.reelview.client.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class AnthropicClient {

    private final String apiKey;
    private final RestClient restClient;

    public AnthropicClient(@Value("${anthropic.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.create("https://api.anthropic.com/v1");
    }

    public String summarize(List<String> comments) {
        String commentsText = String.join("\n- ", comments);
        String prompt = "다음은 유튜브 리뷰 영상에 달린 댓글 목록이야. 시청자들의 전반적인 반응과 의견을 한국어로 3~4문장으로 요약해줘. 댓글 원문을 그대로 나열하지 말고 자연스러운 요약문으로 작성해줘.\n\n- "
                + commentsText;

        AnthropicMessageRequest request = new AnthropicMessageRequest(
                "claude-haiku-4-5-20251001",
                512,
                List.of(new AnthropicMessageRequest.Message("user", prompt))
        );

        AnthropicMessageResponse response = restClient.post()
                .uri("/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .body(request)
                .retrieve()
                .body(AnthropicMessageResponse.class);

        if (response == null || response.getContent() == null || response.getContent().isEmpty()) {
            throw new IllegalStateException("댓글 요약 생성에 실패했습니다.");
        }

        return response.getContent().get(0).getText();
    }
}
