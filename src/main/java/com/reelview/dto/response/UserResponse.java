package com.reelview.dto.response;

import com.reelview.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Long userId;
    private String username;
    private String email;
    private String nickname;
    private LocalDateTime createdAt;
    private Role role;

}
