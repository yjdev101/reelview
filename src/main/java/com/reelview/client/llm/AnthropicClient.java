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
        String prompt = "다음은 유튜브 리뷰 영상에 달린 댓글 목록이야. 시청자 반응을 요약해줘. 아래 규칙을 반드시 지켜야 해.\n"
                + "1. 정확히 2~3문장으로만 작성한다. 그 이상 쓰지 않는다.\n"
                + "2. 마크다운 헤더(#), 제목, 인사말 없이 요약 문장으로 바로 시작한다.\n"
                + "3. 핵심 키워드 3~5개를 **키워드** 형태로 감싸서 강조한다.\n"
                + "4. 댓글 원문을 그대로 나열하지 말고 자연스러운 문장으로 종합한다.\n\n"
                + "댓글 목록:\n- " + commentsText;

        AnthropicMessageRequest request = new AnthropicMessageRequest(
                "claude-haiku-4-5-20251001",
                256,
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

        return stripMarkdownHeader(response.getContent().get(0).getText());
    }

    private String stripMarkdownHeader(String text) {
        return text.replaceAll("(?m)^#{1,6}\\s.*\\R*", "").strip();
    }
}
