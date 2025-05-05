package com.example.LearningMedia.repository;

import com.example.LearningMedia.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<Post> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    List<Post> findByUserIdInOrderByCreatedAtDesc(List<String> userIds, Pageable pageable);
} 
// Post repository