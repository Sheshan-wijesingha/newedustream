package com.example.LearningMedia.controller;

import com.example.LearningMedia.model.User;
import com.example.LearningMedia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserApiController {
    
    private final UserService userService;
    
    @Autowired
    public UserApiController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Follow a user to system
     */
    @PostMapping("/{userId}/follow")
    public ResponseEntity<Map<String, Object>> followUser(@PathVariable String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        Optional<User> currentUserOpt = userService.findByEmail(email);
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Current user not found"
            ));
        }
        
        User currentUser = currentUserOpt.get();
        
        // Prevent self-following (cannot follow yourself)
        if (currentUser.getId().equals(userId)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "You cannot follow yourself"
            ));
        }
        
        // Check if target user exists
        if (!userService.findById(userId).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "User to follow not found"
            ));
        }
        
        boolean followed = userService.followUser(currentUser.getId(), userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", followed);
        
        if (followed) {
            response.put("message", "User followed successfully");
            response.put("followersCount", userService.getFollowersCount(userId));
        } else {
            response.put("message", "Failed to follow user");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Unfollow a user
     */
    @PostMapping("/{userId}/unfollow")
    public ResponseEntity<Map<String, Object>> unfollowUser(@PathVariable String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        Optional<User> currentUserOpt = userService.findByEmail(email);
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Current user not found"
            ));
        }
        
        User currentUser = currentUserOpt.get();
        
        // Check if target user exists
        if (!userService.findById(userId).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "User to unfollow not found"
            ));
        }
        
        boolean unfollowed = userService.unfollowUser(currentUser.getId(), userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", unfollowed);
        
        if (unfollowed) {
            response.put("message", "User unfollowed successfully");
            response.put("followersCount", userService.getFollowersCount(userId));
        } else {
            response.put("message", "Failed to unfollow user");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get followers of a user (followers list)
     */
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<User>> getFollowers(@PathVariable String userId) {
        List<User> followers = userService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }
    
    /**
     * Get users that a user is following (following list)
     */
    @GetMapping("/{userId}/following")
    public ResponseEntity<List<User>> getFollowing(@PathVariable String userId) {
        List<User> following = userService.getFollowing(userId);
        return ResponseEntity.ok(following);
    }
} 