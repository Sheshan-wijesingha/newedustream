package com.example.LearningMedia.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
public class Notification {
    
    @Id
    private String id;
    
    private String userId;  
    private String senderId; 
    private String senderName; 
    private String senderProfilePicture; 
    
    private String targetId; // ID of the related object (post, comment, learning plan)
    private NotificationType type; // Type of notification
    private String content; // Short description
    private boolean read = false; // Whether the notification has been read
    private LocalDateTime createdAt;
    
    public enum NotificationType {
        POST_LIKE,
        POST_COMMENT,
        LEARNING_PLAN_COMPLETED
    }
    
    // Default constructor
    public Notification() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with required fields
    public Notification(String userId, String senderId, String senderName, String senderProfilePicture, 
                     String targetId, NotificationType type, String content) {
        this();
        this.userId = userId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderProfilePicture = senderProfilePicture;
        this.targetId = targetId;
        this.type = type;
        this.content = content;
    }
    
    // Getters and Setters
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
    
    public String getSenderId() {
        return senderId;
    }
    
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public String getSenderProfilePicture() {
        return senderProfilePicture;
    }
    
    public void setSenderProfilePicture(String senderProfilePicture) {
        this.senderProfilePicture = senderProfilePicture;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 