package com.example.LearningMedia.service.impl;

import com.example.LearningMedia.model.LearningPlan;
import com.example.LearningMedia.model.Notification;
import com.example.LearningMedia.model.Notification.NotificationType;
import com.example.LearningMedia.model.Post;
import com.example.LearningMedia.model.User;
import com.example.LearningMedia.repository.LearningPlanRepository;
import com.example.LearningMedia.repository.NotificationRepository;
import com.example.LearningMedia.repository.PostRepository;
import com.example.LearningMedia.repository.UserRepository;
import com.example.LearningMedia.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LearningPlanRepository learningPlanRepository;
    
    @Autowired
    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            PostRepository postRepository,
            LearningPlanRepository learningPlanRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.learningPlanRepository = learningPlanRepository;
    }
    
    @Override
    @Transactional
    public Notification createPostLikeNotification(String postId, String likerId) {
        try {
            // Get the post
            Optional<Post> postOpt = postRepository.findById(postId);
            if (postOpt.isEmpty()) {
                logger.error("Cannot create notification: Post with ID {} not found", postId);
                return null;
            }
            
            Post post = postOpt.get();
            String postOwnerId = post.getUserId();
            
            // Don't notify if user likes their own post
            if (postOwnerId.equals(likerId)) {
                return null;
            }
            
            // Get the liker
            Optional<User> likerOpt = userRepository.findById(likerId);
            if (likerOpt.isEmpty()) {
                logger.error("Cannot create notification: User (liker) with ID {} not found", likerId);
                return null;
            }
            
            User liker = likerOpt.get();
            
            // Create the notification
            String content = liker.getFullName() + " liked your post";
            Notification notification = new Notification(
                    postOwnerId,
                    likerId,
                    liker.getFullName(),
                    liker.getProfilePicture(),
                    postId,
                    NotificationType.POST_LIKE,
                    content
            );
            
            return notificationRepository.save(notification);
        } catch (Exception e) {
            logger.error("Error creating post like notification", e);
            return null;
        }
    }
    
    @Override
    @Transactional
    public Notification createPostCommentNotification(String postId, String commenterId, String commentContent) {
        try {
            // Get the post
            Optional<Post> postOpt = postRepository.findById(postId);
            if (postOpt.isEmpty()) {
                logger.error("Cannot create notification: Post with ID {} not found", postId);
                return null;
            }
            
            Post post = postOpt.get();
            String postOwnerId = post.getUserId();
            
            // Don't notify if user comments on their own post
            if (postOwnerId.equals(commenterId)) {
                return null;
            }
            
            // Get the commenter
            Optional<User> commenterOpt = userRepository.findById(commenterId);
            if (commenterOpt.isEmpty()) {
                logger.error("Cannot create notification: User (commenter) with ID {} not found", commenterId);
                return null;
            }
            
            User commenter = commenterOpt.get();
            
            // Create the notification with a snippet of the comment
            String truncatedComment = commentContent.length() > 30 
                ? commentContent.substring(0, 27) + "..." 
                : commentContent;
            
            String content = commenter.getFullName() + " commented on your post: \"" + truncatedComment + "\"";
            Notification notification = new Notification(
                    postOwnerId,
                    commenterId,
                    commenter.getFullName(),
                    commenter.getProfilePicture(),
                    postId,
                    NotificationType.POST_COMMENT,
                    content
            );
            
            return notificationRepository.save(notification);
        } catch (Exception e) {
            logger.error("Error creating post comment notification", e);
            return null;
        }
    }
    
    @Override
    @Transactional
    public Notification createLearningPlanCompletedNotification(String learningPlanId, String userId) {
        try {
            // Get the learning plan
            Optional<LearningPlan> planOpt = learningPlanRepository.findById(learningPlanId);
            if (planOpt.isEmpty()) {
                logger.error("Cannot create notification: Learning Plan with ID {} not found", learningPlanId);
                return null;
            }
            
            LearningPlan plan = planOpt.get();
            
            // Verify the plan belongs to the user
            if (!plan.getUserId().equals(userId)) {
                logger.error("Cannot create notification: Learning Plan {} does not belong to user {}", learningPlanId, userId);
                return null;
            }
            
            // Create the notification (system notification, no sender)
            String content = "You have completed your learning plan: \"" + plan.getTitle() + "\"";
            Notification notification = new Notification(
                    userId,
                    null, // No sender (system notification)
                    "Learning Media",
                    null, // No profile picture
                    learningPlanId,
                    NotificationType.LEARNING_PLAN_COMPLETED,
                    content
            );
            
            return notificationRepository.save(notification);
        } catch (Exception e) {
            logger.error("Error creating learning plan completed notification", e);
            return null;
        }
    }
    
    @Override
    public Page<Notification> getUserNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    @Override
    @Transactional
    public Notification markNotificationAsRead(String notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            return null;
        }
        
        Notification notification = notificationOpt.get();
        notification.setRead(true);
        return notificationRepository.save(notification);
    }
    
    @Override
    @Transactional
    public void markAllNotificationsAsRead(String userId) {
        // Get all notifications for the user
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged());
        
        // Mark each as read
        notifications.forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
    
    @Override
    public long countUnreadNotifications(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
    
    @Override
    @Transactional
    public void deleteNotification(String notificationId, String userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent() && notificationOpt.get().getUserId().equals(userId)) {
            notificationRepository.deleteById(notificationId);
        }
    }
    
    @Override
    @Transactional
    public void deleteAllNotificationsForUser(String userId) {
        // Find all notifications for the user
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged());
        
        // Delete each notification individually
        notifications.forEach(notification -> {
            notificationRepository.deleteById(notification.getId());
        });
        
        logger.info("Deleted all notifications for user with ID: {}", userId);
    }
} 