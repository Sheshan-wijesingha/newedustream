package com.example.LearningMedia.dto;

import com.example.LearningMedia.model.Post;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PostResponse {
    
    private String id;
    private String userId;
    private String userFirstName;
    private String userLastName;
    private String userFullName;
    private String userProfilePicture;
    private String description;
    private List<MediaItem> mediaItems = new ArrayList<>();
    private int commentCount;
    private List<CommentResponse> comments = new ArrayList<>();
    private LocalDateTime createdAt;
    private String timeAgo;
    private boolean canEdit;
    
    
    private int likeCount;
    private boolean likedByCurrentUser;
    
    public PostResponse() {
    }
    
    public static PostResponse fromPost(Post post, String currentUserId) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setUserId(post.getUserId());
        response.setUserFirstName(post.getUserFirstName());
        response.setUserLastName(post.getUserLastName());
        response.setUserFullName(post.getUserFirstName() + " " + post.getUserLastName());
        response.setUserProfilePicture(post.getUserProfilePicture());
        response.setDescription(post.getDescription());
        response.setCommentCount(0);
        response.setCreatedAt(post.getCreatedAt());
        
        // Set canEdit flag - true if current user is the creator of the post
        response.setCanEdit(post.getUserId().equals(currentUserId));
        
        // Set like-related fields
        response.setLikeCount(post.getLikeCount());
        response.setLikedByCurrentUser(post.isLikedByUser(currentUserId));
        
        // Convert Post.Media to MediaItem
        if (post.getMediaItems() != null) {
            response.setMediaItems(
                post.getMediaItems().stream()
                    .map(media -> {
                        MediaItem item = new MediaItem();
                        item.setId(media.getId());
                        item.setUrl(media.getUrl());
                        item.setType(media.getType());
                        item.setContentType(media.getContentType());
                        return item;
                    })
                    .collect(Collectors.toList())
            );
        }
        
        // Calculate time ago
        response.setTimeAgo(calculateTimeAgo(post.getCreatedAt()));
        
        return response;
    }
    
    public static PostResponse fromPost(Post post, String currentUserId, int commentCount) {
        PostResponse response = fromPost(post, currentUserId);
        response.setCommentCount(commentCount);
        return response;
    }
    
    public static PostResponse fromPost(Post post, String currentUserId, List<CommentResponse> comments) {
        PostResponse response = fromPost(post, currentUserId);
        response.setComments(comments);
        response.setCommentCount(comments.size());
        return response;
    }
    
    private static String calculateTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(dateTime, now).getSeconds();
        
        if (seconds < 60) {
            return "just now";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else {
            long days = seconds / 86400;
            return days + (days == 1 ? " day ago" : " days ago");
        }
    }
    
    public static class MediaItem {
        private String id;
        private String url;
        private String type;
        private String contentType;
        
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
    
    public String getUserFullName() {
        return userFullName;
    }
    
    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
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
    
    public List<MediaItem> getMediaItems() {
        return mediaItems;
    }
    
    public void setMediaItems(List<MediaItem> mediaItems) {
        this.mediaItems = mediaItems;
    }
    
    public int getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getTimeAgo() {
        return timeAgo;
    }
    
    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }
    
    public boolean isCanEdit() {
        return canEdit;
    }
    
    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }
    
    public List<CommentResponse> getComments() {
        return comments;
    }
    
    public void setComments(List<CommentResponse> comments) {
        this.comments = comments;
    }
    
    public int getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
    
    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }
    
    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }
} 