package com.reelview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(length = 500)
    private String refreshToken;
    private LocalDateTime refreshTokenExpiry;

}
