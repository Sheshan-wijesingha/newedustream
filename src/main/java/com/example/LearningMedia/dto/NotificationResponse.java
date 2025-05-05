package com.example.LearningMedia.dto;

import com.example.LearningMedia.model.Notification;
import java.time.LocalDateTime;

public class NotificationResponse {
    private String id;
    private String senderId;
    private String senderName;
    private String senderProfilePicture;
    private String targetId;
    private String type;
    private String content;
    private boolean read;
    private LocalDateTime createdAt;
    
    public NotificationResponse() {
    }
    
    // Factory method to convert from Notification entity to NotificationResponse
    public static NotificationResponse fromNotification(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setSenderId(notification.getSenderId());
        response.setSenderName(notification.getSenderName());
        response.setSenderProfilePicture(notification.getSenderProfilePicture());
        response.setTargetId(notification.getTargetId());
        response.setType(notification.getType().name());
        response.setContent(notification.getContent());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
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