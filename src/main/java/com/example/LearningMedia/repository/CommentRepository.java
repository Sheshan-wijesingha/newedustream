package com.example.LearningMedia.repository;

import com.example.LearningMedia.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByPostIdOrderByCreatedAtAsc(String postId);
    Page<Comment> findByPostIdOrderByCreatedAtAsc(String postId, Pageable pageable);
    List<Comment> findByUserId(String userId);
    void deleteByPostId(String postId);
} 