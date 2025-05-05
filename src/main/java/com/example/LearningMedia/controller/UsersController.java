package com.example.LearningMedia.controller;

import com.example.LearningMedia.model.User;
import com.example.LearningMedia.service.UserService;
import com.example.LearningMedia.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UsersController {
    
    private final UserService userService;
    private final NotificationService notificationService;
    
    @Autowired
    public UsersController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }
    
    /**
     * Display the users discovery page with all users or search results
     */
    @GetMapping
    public String discoverUsers(
            @RequestParam(required = false) String query, 
            Model model, 
            Principal principal) {
        
        if (principal == null) {
            return "redirect:/login";
        }

        // Get current user discovery page
        Optional<User> currentUserOpt = userService.findByEmail(principal.getName());
        if (currentUserOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        User currentUser = currentUserOpt.get();
        model.addAttribute("currentUser", currentUser);
        
        // Add notification count to current user discovery page
        long unreadNotificationCount = notificationService.countUnreadNotifications(currentUser.getId());
        model.addAttribute("unreadNotificationCount", unreadNotificationCount);
        
        // Get users based on search query or get all users
        List<User> users;
        if (query != null && !query.trim().isEmpty()) {
            users = userService.searchUsers(query);
            model.addAttribute("searchQuery", query);
        } else {
            users = userService.getAllUsersExceptCurrent(currentUser.getId());
        }
        
        // For each user, check if current user is following them
        users.forEach(user -> {
            user.setCurrentUserFollowing(
                userService.isFollowing(currentUser.getId(), user.getId())
            );
        });
        
        model.addAttribute("users", users);
        
        return "users/discover";
    }
} 