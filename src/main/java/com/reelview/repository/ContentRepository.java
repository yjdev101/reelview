package com.reelview.repository;

import com.reelview.entity.Content;
import com.reelview.entity.ContentType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content,Long> {
    List<Content> findByGenres_Name(String genreName, Sort sort);
    List<Content> findByType(ContentType type, Sort sort);
    Optional<Content> findByTmdbId(Long tmdbId);

    @Query("SELECT c FROM Content c " +
            "LEFT JOIN Review r ON r.content = c " +
            "LEFT JOIN ReviewRating rr ON rr.review = r " +
            "GROUP BY c " +
            "ORDER BY AVG(rr.rating) DESC")
        List<Content> findAllOrderByAverageRatingDesc();

}