package com.example.LearningMedia.repository;

import com.example.LearningMedia.model.UserFollow;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// User follow repository
@Repository
public interface UserFollowRepository extends MongoRepository<UserFollow, String> {
    
    List<UserFollow> findByFollowerId(String followerId);
    
    List<UserFollow> findByFollowingId(String followingId);
    
    Optional<UserFollow> findByFollowerIdAndFollowingId(String followerId, String followingId);
    
    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);
    
    long countByFollowingId(String followingId);
    
    long countByFollowerId(String followerId);
    
    void deleteByFollowerIdAndFollowingId(String followerId, String followingId);
    
    void deleteByFollowerId(String followerId);
    
    void deleteByFollowingId(String followingId);
} 