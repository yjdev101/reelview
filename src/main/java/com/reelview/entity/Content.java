package com.reelview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "content")
@Getter
@Setter
@NoArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContentType type;

    @Column(nullable = false)
    private Integer releaseYear;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "content_genre",
            joinColumns = @JoinColumn(name = "content_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

}
