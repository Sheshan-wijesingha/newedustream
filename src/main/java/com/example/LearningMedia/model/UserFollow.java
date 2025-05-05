package com.example.LearningMedia.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "user_follows")
@CompoundIndexes({
    @CompoundIndex(name = "follower_following_idx", def = "{'followerId': 1, 'followingId': 1}", unique = true)
})
public class UserFollow {
    
    @Id
    private String id;
    
    private String followerId;
    
    private String followingId;
    
    private LocalDateTime createdAt;
    
    // Default constructor
    public UserFollow() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with required fields
    public UserFollow(String followerId, String followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getFollowerId() {
        return followerId;
    }
    
    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }
    
    public String getFollowingId() {
        return followingId;
    }
    
    public void setFollowingId(String followingId) {
        this.followingId = followingId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 