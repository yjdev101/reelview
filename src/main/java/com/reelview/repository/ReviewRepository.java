package com.reelview.repository;

import com.reelview.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review,Long> {
    List<Review> findByContentId(Long contentId);
}
