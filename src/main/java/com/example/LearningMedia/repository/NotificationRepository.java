package com.example.LearningMedia.repository;

import com.example.LearningMedia.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    // Find notifications for a specific user
    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    // Count unread notifications for a user
    long countByUserIdAndReadFalse(String userId);
    
    // Delete notifications by user and target ID
    void deleteByUserIdAndTargetId(String userId, String targetId);
    
    // Delete notifications by user
    void deleteByUserId(String userId);
} 