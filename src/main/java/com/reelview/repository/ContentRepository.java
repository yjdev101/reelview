package com.reelview.repository;

import com.reelview.entity.Content;
import com.reelview.entity.ContentType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content,Long> {
    List<Content> findByGenres_Name(String genreName, Sort sort);
    List<Content> findByType(ContentType type, Sort sort);
}
