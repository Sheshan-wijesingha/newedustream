package com.example.LearningMedia.service;

import com.example.LearningMedia.model.Notification;
import com.example.LearningMedia.model.Notification.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    
    // Create a notification when someone likes a post
    Notification createPostLikeNotification(String postId, String likerId);
    
    // Create a notification when someone comments on a post
    Notification createPostCommentNotification(String postId, String commenterId, String commentContent);
    
    // Create a notification when a learning plan is completed
    Notification createLearningPlanCompletedNotification(String learningPlanId, String userId);
    
    // Get notifications for a user
    Page<Notification> getUserNotifications(String userId, Pageable pageable);
    
    // Mark a notification as read
    Notification markNotificationAsRead(String notificationId);
    
    // Mark all notifications as read for a user
    void markAllNotificationsAsRead(String userId);
    
    // Count unread notifications for a user
    long countUnreadNotifications(String userId);
    
    // Delete a notification
    void deleteNotification(String notificationId, String userId);
    
    /**
     * Delete all notifications for a specific user
     * 
     * @param userId The ID of the user whose notifications will be deleted
     */
    void deleteAllNotificationsForUser(String userId);
} 