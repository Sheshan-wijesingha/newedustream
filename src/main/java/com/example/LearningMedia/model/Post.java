package com.example.LearningMedia.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "posts")
public class Post {
    
    @Id
    private String id;
    
    private String userId;
    private String userFirstName;
    private String userLastName;
    private String userProfilePicture;
    
    private String description;
    
    private List<Media> mediaItems = new ArrayList<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Track likes by storing user IDs who liked the post
    private Set<String> likedByUsers = new HashSet<>();
    
    public Post() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public static class Media {
        private String id;
        private String url;
        private String type; // "image" or "video"
        private String contentType;
        private LocalDateTime uploadedAt;
        
        public Media() {
            this.id = java.util.UUID.randomUUID().toString();
            this.uploadedAt = LocalDateTime.now();
        }
        
        public Media(String url, String type, String contentType) {
            this();
            this.url = url;
            this.type = type;
            this.contentType = contentType;
        }
        
        // Getters and setters
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getContentType() {
            return contentType;
        }
        
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
        
        public LocalDateTime getUploadedAt() {
            return uploadedAt;
        }
        
        public void setUploadedAt(LocalDateTime uploadedAt) {
            this.uploadedAt = uploadedAt;
        }
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserFirstName() {
        return userFirstName;
    }
    
    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }
    
    public String getUserLastName() {
        return userLastName;
    }
    
    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }
    
    public String getUserProfilePicture() {
        return userProfilePicture;
    }
    
    public void setUserProfilePicture(String userProfilePicture) {
        this.userProfilePicture = userProfilePicture;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<Media> getMediaItems() {
        return mediaItems;
    }
    
    public void setMediaItems(List<Media> mediaItems) {
        this.mediaItems = mediaItems;
    }
    
    public void addMediaItem(Media media) {
        this.mediaItems.add(media);
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Like-related methods
    public Set<String> getLikedByUsers() {
        return likedByUsers;
    }
    
    public void setLikedByUsers(Set<String> likedByUsers) {
        this.likedByUsers = likedByUsers;
    }
    
    public boolean addLike(String userId) {
        return this.likedByUsers.add(userId);
    }
    
    public boolean removeLike(String userId) {
        return this.likedByUsers.remove(userId);
    }
    
    public boolean isLikedByUser(String userId) {
        return this.likedByUsers.contains(userId);
    }
    
    public int getLikeCount() {
        return this.likedByUsers.size();
    }
    
    // Utility methods for media handling
    public boolean hasMedia() {
        return !this.mediaItems.isEmpty();
    }
    
    public boolean isAtMediaLimit() {
        return this.mediaItems.size() >= 3;
    }
    
    public int getMediaCount() {
        return this.mediaItems.size();
    }
} 