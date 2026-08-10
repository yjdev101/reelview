package com.reelview.controller;

import com.reelview.dto.request.LoginRequest;
import com.reelview.dto.request.RegisterRequest;
import com.reelview.dto.request.ReissueRequest;
import com.reelview.dto.response.LoginResponse;
import com.reelview.dto.response.ReissueResponse;
import com.reelview.dto.response.UserResponse;
import com.reelview.entity.User;
import com.reelview.security.JwtUtil;
import com.reelview.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService,  JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public UserResponse registerUser(@RequestBody RegisterRequest request) {
        User user = userService.register(request.getUsername(), request.getPassword(), request.getEmail(), request.getNickname());
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getNickname(), user.getCreatedAt(), user.getRole());
    }

    @PostMapping("/login")
    public LoginResponse loginUser(@RequestBody LoginRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        LocalDateTime expiry = LocalDateTime.now().plus(jwtUtil.getRefreshExpiration(), ChronoUnit.MILLIS);
        userService.saveRefreshToken(user, refreshToken, expiry);
        return new LoginResponse(token, refreshToken);
    }

    @PostMapping("/logout")
    public void logout(Authentication authentication) {
        userService.logout(authentication.getName());
    }

    @PostMapping("/reissue")
    public ReissueResponse reissue(@RequestBody ReissueRequest request) {
        String refreshToken = request.getRefreshToken();
        String username = jwtUtil.extractUsername(refreshToken);
        User user = userService.validateRefreshToken(username, refreshToken);
        String newToken = jwtUtil.generateToken(username, user.getRole());
        return new ReissueResponse(newToken);
    }
}
