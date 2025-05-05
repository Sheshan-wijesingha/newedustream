package com.example.LearningMedia.controller;

import com.example.LearningMedia.dto.CommentRequest;
import com.example.LearningMedia.dto.CommentResponse;
import com.example.LearningMedia.model.Comment;
import com.example.LearningMedia.model.User;
import com.example.LearningMedia.service.CommentService;
import com.example.LearningMedia.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    
    private final CommentService commentService;
    private final UserService userService;
    
    @Autowired
    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }
    
    @PostMapping("/post/{postId}")
    public ResponseEntity<?> createComment(
            @PathVariable String postId,
            @Valid @RequestBody CommentRequest commentRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            String userId = userOptional.get().getId();
            
            Comment comment = commentService.createComment(postId, userId, commentRequest.getContent());
            
            CommentResponse response = CommentResponse.fromComment(comment, userId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating comment", e);
            return ResponseEntity.badRequest().body("Failed to create comment: " + e.getMessage());
        }
    }
    
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(
            @PathVariable String postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            String userId = userOptional.get().getId();
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Comment> comments = commentService.getCommentsByPostId(postId, pageable);
            
            List<CommentResponse> commentResponses = comments.getContent().stream()
                    .map(comment -> CommentResponse.fromComment(comment, userId))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(commentResponses);
        } catch (Exception e) {
            logger.error("Error fetching comments for post {}", postId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable String commentId,
            @Valid @RequestBody CommentRequest commentRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            String userId = userOptional.get().getId();
            
            // Check if user is the owner of the comment
            if (!commentService.isCommentOwner(commentId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only edit your own comments");
            }
            
            Comment updatedComment = commentService.updateComment(commentId, commentRequest.getContent());
            
            CommentResponse response = CommentResponse.fromComment(updatedComment, userId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating comment {}", commentId, e);
            return ResponseEntity.badRequest().body("Failed to update comment: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable String commentId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            String userId = userOptional.get().getId();
            
            // Check if user is the owner of the comment
            if (!commentService.isCommentOwner(commentId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only delete your own comments");
            }
            
            commentService.deleteComment(commentId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Comment deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting comment {}", commentId, e);
            return ResponseEntity.badRequest().body("Failed to delete comment: " + e.getMessage());
        }
    }
} 