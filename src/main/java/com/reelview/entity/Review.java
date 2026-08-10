package com.reelview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReviewType reviewType;

    @Column(length = 500)
    private String videoUrl;

    @Column(length = 500)
    private String videoFilePath;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String commentSummary;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
