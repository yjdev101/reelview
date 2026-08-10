package com.reelview.controller;

import com.reelview.entity.*;
import com.reelview.service.ContentService;
import com.reelview.service.ReviewService;
import com.reelview.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ContentService contentService;

    @Test
    @WithMockUser
    void getReview_성공() throws Exception{

        // 1.준비
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setNickname("테스트유저");

        Content content = new Content();
        content.setId(1L);
        content.setTitle("인터스텔라");
        content.setType(ContentType.MOVIE);

        Review review = new Review();
        review.setId(1L);
        review.setUser(user);
        review.setContent(content);
        review.setTitle("최고의 SF영화");
        review.setReviewType(ReviewType.UPLOAD);
        review.setDescription("정말 감동적이었습니다.");

        //2.조건
        when(reviewService.getReview(1L)).thenReturn(review);

        //3.실행
        mockMvc.perform(get("/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("최고의 SF영화"));
    }
}
