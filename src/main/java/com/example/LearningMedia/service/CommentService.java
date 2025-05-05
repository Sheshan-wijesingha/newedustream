package com.example.LearningMedia.service;

import com.example.LearningMedia.model.Comment;
import com.example.LearningMedia.model.User;
import com.example.LearningMedia.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public CommentService(CommentRepository commentRepository, UserService userService, 
                          NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    public Comment createComment(String postId, String userId, String content) {
        Optional<User> userOptional = userService.findById(userId);
        
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        User user = userOptional.get();
        
        Comment comment = new Comment(
            postId,
            userId,
            user.getFirstName(),
            user.getLastName(),
            user.getProfilePicture(),
            content
        );
        
        Comment savedComment = commentRepository.save(comment);
        
        // Create notification for post owner
        notificationService.createPostCommentNotification(postId, userId, content);
        
        return savedComment;
    }

    public List<Comment> getCommentsByPostId(String postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }
    
    public Page<Comment> getCommentsByPostId(String postId, Pageable pageable) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId, pageable);
    }

    public Optional<Comment> getCommentById(String commentId) {
        return commentRepository.findById(commentId);
    }

    public Comment updateComment(String commentId, String content) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        
        if (optionalComment.isEmpty()) {
            throw new IllegalArgumentException("Comment not found");
        }
        
        Comment comment = optionalComment.get();
        comment.setContent(content);
        comment.setUpdatedAt(LocalDateTime.now());
        
        return commentRepository.save(comment);
    }

    public void deleteComment(String commentId) {
        commentRepository.deleteById(commentId);
    }
    
    public void deleteCommentsByPostId(String postId) {
        commentRepository.deleteByPostId(postId);
    }
    
    public boolean isCommentOwner(String commentId, String userId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        return optionalComment.filter(comment -> comment.getUserId().equals(userId)).isPresent();
    }
} 