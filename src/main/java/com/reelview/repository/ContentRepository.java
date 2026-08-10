package com.reelview.repository;

import com.reelview.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content,Long> {
}
