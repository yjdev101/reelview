package com.reelview.service;

import com.reelview.entity.Role;
import com.reelview.entity.User;
import com.reelview.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String password, String email, String nickname) {
        User user = new User();

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setNickname(nickname);
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("로그인에 실패했습니다.");
        }

        return user;
    }

    public void logout(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    public void saveRefreshToken(User user, String refreshToken, LocalDateTime expiry) {
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(expiry);
        userRepository.save(user);
    }

    public User validateRefreshToken(String username, String refreshToken) {
        User user = userRepository.findByUsername(username).orElseThrow();
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new IllegalArgumentException("유효하지 않은 refresh token입니다.");
        } else {
            return user;
        }
    }
}
