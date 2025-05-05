package com.example.LearningMedia.controller;

import com.example.LearningMedia.dto.PostRequest;
import com.example.LearningMedia.dto.PostResponse;
import com.example.LearningMedia.model.LearningPlan;
import com.example.LearningMedia.model.Post;
import com.example.LearningMedia.model.User;
import com.example.LearningMedia.service.LearningPlanService;
import com.example.LearningMedia.service.NotificationService;
import com.example.LearningMedia.service.PostService;
import com.example.LearningMedia.service.UserService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
// Profile controller
@Controller
@RequestMapping("/profile")
public class ProfileController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    
    private final UserService userService;
    private final PostService postService;
    private final PasswordEncoder passwordEncoder;
    private final LearningPlanService learningPlanService;
    private final NotificationService notificationService;
    
    @Autowired
    public ProfileController(UserService userService, PostService postService, 
                            PasswordEncoder passwordEncoder, LearningPlanService learningPlanService,
                            NotificationService notificationService) {
        this.userService = userService;
        this.postService = postService;
        this.passwordEncoder = passwordEncoder;
        this.learningPlanService = learningPlanService;
        this.notificationService = notificationService;
    }
    
    // View own profile details
    @GetMapping
    public String viewOwnProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        // Get user by email from principal
        Optional<User> userOpt = userService.findByEmail(principal.getName());
        
        // Check if this is an OAuth login
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (userOpt.isEmpty() && auth instanceof OAuth2AuthenticationToken) {
            // Process OAuth user
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            String email = principal.getName();
            
            // Get name from OAuth2 attributes
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String name = "";
            
            if (attributes.containsKey("name")) {
                name = (String) attributes.get("name");
            } else if (attributes.containsKey("given_name") && attributes.containsKey("family_name")) {
                name = attributes.get("given_name") + " " + attributes.get("family_name");
            }
            
            // Create or retrieve user
            User user = userService.processOAuthPostLogin(email, name, oauthToken.getAuthorizedClientRegistrationId());
            return showProfilePage(model, user, true, user);
        }
        
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        User user = userOpt.get();
        return showProfilePage(model, user, true, user);
    }

    // View profile by username
    @GetMapping("/{username}")
    public String viewProfileByUsername(@PathVariable String username, Model model, Principal principal) {
        Optional<User> profileUserOpt = userService.findByUsername(username);
        if (profileUserOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        User profileUser = profileUserOpt.get();
        boolean isOwnProfile = false;
        User currentUser = null;

        if (principal != null) {
            Optional<User> currentUserOpt = userService.findByEmail(principal.getName());
            
            // Handle OAuth user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (currentUserOpt.isEmpty() && auth instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
                OAuth2User oAuth2User = oauthToken.getPrincipal();
                String email = principal.getName();
                
                // Get name from OAuth2 attributes
                Map<String, Object> attributes = oAuth2User.getAttributes();
                String name = "";
                
                if (attributes.containsKey("name")) {
                    name = (String) attributes.get("name");
                } else if (attributes.containsKey("given_name") && attributes.containsKey("family_name")) {
                    name = attributes.get("given_name") + " " + attributes.get("family_name");
                }
                
                // Create or retrieve user
                currentUser = userService.processOAuthPostLogin(email, name, oauthToken.getAuthorizedClientRegistrationId());
                isOwnProfile = currentUser.getId().equals(profileUser.getId());
                
                // Set following status if not own profile
                if (!isOwnProfile) {
                    model.addAttribute("isFollowing", 
                            userService.isFollowing(currentUser.getId(), profileUser.getId()));
                }
            } else if (currentUserOpt.isPresent()) {
                currentUser = currentUserOpt.get();
                isOwnProfile = currentUser.getId().equals(profileUser.getId());
                
                // Set following status if not own profile
                if (!isOwnProfile) {
                    model.addAttribute("isFollowing", 
                            userService.isFollowing(currentUser.getId(), profileUser.getId()));
                }
            }
        }

        return showProfilePage(model, profileUser, isOwnProfile, currentUser);
    }

    // View profile by user ID
    @GetMapping("/user/{userId}")
    public String viewProfileById(@PathVariable String userId, Model model, Principal principal) {
        Optional<User> profileUserOpt = userService.findById(userId);
        if (profileUserOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        User profileUser = profileUserOpt.get();
        
        // If user has username, redirect to username URL for better SEO
        if (profileUser.getUsername() != null && !profileUser.getUsername().isEmpty()) {
            return "redirect:/profile/" + profileUser.getUsername();
        }

        boolean isOwnProfile = false;
        User currentUser = null;

        if (principal != null) {
            Optional<User> currentUserOpt = userService.findByEmail(principal.getName());
            
            // Handle OAuth user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (currentUserOpt.isEmpty() && auth instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
                OAuth2User oAuth2User = oauthToken.getPrincipal();
                String email = principal.getName();
                
                // Get name from OAuth2 attributes
                Map<String, Object> attributes = oAuth2User.getAttributes();
                String name = "";
                
                if (attributes.containsKey("name")) {
                    name = (String) attributes.get("name");
                } else if (attributes.containsKey("given_name") && attributes.containsKey("family_name")) {
                    name = attributes.get("given_name") + " " + attributes.get("family_name");
                }
                
                // Create or retrieve user
                currentUser = userService.processOAuthPostLogin(email, name, oauthToken.getAuthorizedClientRegistrationId());
                isOwnProfile = currentUser.getId().equals(profileUser.getId());
                
                // Set following status if not own profile
                if (!isOwnProfile) {
                    model.addAttribute("isFollowing", 
                            userService.isFollowing(currentUser.getId(), profileUser.getId()));
                }
            } else if (currentUserOpt.isPresent()) {
                currentUser = currentUserOpt.get();
                isOwnProfile = currentUser.getId().equals(profileUser.getId());
                
                // Set following status if not own profile
                if (!isOwnProfile) {
                    model.addAttribute("isFollowing", 
                            userService.isFollowing(currentUser.getId(), profileUser.getId()));
                }
            }
        }

        return showProfilePage(model, profileUser, isOwnProfile, currentUser);
    }

    // Helper method to prepare the profile page
    private String showProfilePage(Model model, User profileUser, boolean isOwnProfile, User currentUser) {
        model.addAttribute("user", profileUser);
        model.addAttribute("isOwnProfile", isOwnProfile);
        model.addAttribute("followerCount", userService.getFollowersCount(profileUser.getId()));
        model.addAttribute("followingCount", userService.getFollowingCount(profileUser.getId()));
        
        // Add current user to model and notification count if available
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
            
            // Add notification count
            long unreadNotificationCount = notificationService.countUnreadNotifications(currentUser.getId());
            model.addAttribute("unreadNotificationCount", unreadNotificationCount);
        }
        
        // Get user's posts
        List<Post> userPosts = postService.findByUserId(profileUser.getId());
        model.addAttribute("posts", userPosts);
        
        // Get user's shared learning plans
        List<LearningPlan> sharedLearningPlans = learningPlanService.getSharedLearningPlansByUserId(profileUser.getId());
        model.addAttribute("sharedLearningPlans", sharedLearningPlans);
        
        return "profile/view";
    }

    // Edit profile page
    @GetMapping("/edit")
    public String editProfilePage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        // Get user by email from principal
        Optional<User> userOpt = userService.findByEmail(principal.getName());
        
        // Check if this is an OAuth login
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (userOpt.isEmpty() && auth instanceof OAuth2AuthenticationToken) {
            // Process OAuth user
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            String email = principal.getName();
            
            // Get name from OAuth2 attributes
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String name = "";
            
            if (attributes.containsKey("name")) {
                name = (String) attributes.get("name");
            } else if (attributes.containsKey("given_name") && attributes.containsKey("family_name")) {
                name = attributes.get("given_name") + " " + attributes.get("family_name");
            }
            
            // Create or retrieve user
            User user = userService.processOAuthPostLogin(email, name, oauthToken.getAuthorizedClientRegistrationId());
            model.addAttribute("user", user);
            
            // Add notification count
            long unreadNotificationCount = notificationService.countUnreadNotifications(user.getId());
            model.addAttribute("unreadNotificationCount", unreadNotificationCount);
            
            return "profile/edit";
        }
        
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        User user = userOpt.get();
        model.addAttribute("user", user);
        
        // Add notification count
        long unreadNotificationCount = notificationService.countUnreadNotifications(user.getId());
        model.addAttribute("unreadNotificationCount", unreadNotificationCount);
        
        return "profile/edit";
    }

    // Process profile update
    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User updatedUser, 
                               @RequestParam(value = "profilePictureFile", required = false) MultipartFile profilePictureFile,
                               Principal principal, 
                               RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            Optional<User> userOpt = userService.findByEmail(principal.getName());
            if (userOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }

            User currentUser = userOpt.get();
            
            // Process profile picture if provided
            if (profilePictureFile != null && !profilePictureFile.isEmpty()) {
                // Check file size (max 5MB)
                if (profilePictureFile.getSize() > 5 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("error", "Profile picture should not exceed 5MB");
                    return "redirect:/profile/edit";
                }
                
                // Check file type
                String contentType = profilePictureFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    redirectAttributes.addFlashAttribute("error", "Only image files are allowed for profile picture");
                    return "redirect:/profile/edit";
                }
                
                // Convert to Base64 and store
                byte[] bytes = profilePictureFile.getBytes();
                String base64Image = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
                updatedUser.setProfilePicture(base64Image);
            } else if (updatedUser.getProfilePicture() == null || updatedUser.getProfilePicture().isEmpty()) {
                // Keep existing profile picture if user didn't upload new one and didn't explicitly remove it
                updatedUser.setProfilePicture(currentUser.getProfilePicture());
            }
            
            userService.updateProfile(currentUser.getId(), updatedUser);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
            
            return "redirect:/profile";
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }

    // REST endpoint to follow a user
    @PostMapping("/follow/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> followUser(@PathVariable String userId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> followerOpt = userService.findByEmail(principal.getName());
        if (followerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User follower = followerOpt.get();
        boolean success = userService.followUser(follower.getId(), userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        if (success) {
            response.put("followerCount", userService.getFollowersCount(userId));
        }
        
        return ResponseEntity.ok(response);
    }

    // REST endpoint to unfollow a user
    @PostMapping("/unfollow/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unfollowUser(@PathVariable String userId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> followerOpt = userService.findByEmail(principal.getName());
        if (followerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User follower = followerOpt.get();
        boolean success = userService.unfollowUser(follower.getId(), userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        if (success) {
            response.put("followerCount", userService.getFollowersCount(userId));
        }
        
        return ResponseEntity.ok(response);
    }
    
    // View followers list
    @GetMapping("/{username}/followers")
    public String viewFollowers(@PathVariable String username, Model model, Principal principal) {
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        User user = userOpt.get();
        List<User> followers = userService.getFollowers(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("usersList", followers);
        model.addAttribute("listType", "followers");
        
        if (principal != null) {
            Optional<User> currentUserOpt = userService.findByEmail(principal.getName());
            if (currentUserOpt.isPresent()) {
                User currentUser = currentUserOpt.get();
                
                // Check if this is the user's own profile
                boolean isOwnProfile = currentUser.getId().equals(user.getId());
                model.addAttribute("isOwnProfile", isOwnProfile);
                
                // Add follow status for each user in the list
                if (!isOwnProfile) {
                    model.addAttribute("isFollowing", 
                            userService.isFollowing(currentUser.getId(), user.getId()));
                }
            }
        }
        
        return "profile/users_list";
    }
    
    // View following list
    @GetMapping("/{username}/following")
    public String viewFollowing(@PathVariable String username, Model model, Principal principal) {
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        User user = userOpt.get();
        List<User> following = userService.getFollowing(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("usersList", following);
        model.addAttribute("listType", "following");
        
        if (principal != null) {
            Optional<User> currentUserOpt = userService.findByEmail(principal.getName());
            if (currentUserOpt.isPresent()) {
                User currentUser = currentUserOpt.get();
                
                // Check if this is the user's own profile
                boolean isOwnProfile = currentUser.getId().equals(user.getId());
                model.addAttribute("isOwnProfile", isOwnProfile);
                
                // Add follow status for each user in the list
                if (!isOwnProfile) {
                    model.addAttribute("isFollowing", 
                            userService.isFollowing(currentUser.getId(), user.getId()));
                }
            }
        }
        
        return "profile/users_list";
    }
    
    @PostMapping("/update-password")
    public String updatePassword(@RequestParam String currentPassword, 
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Check if user is using OAuth2
            if (authentication instanceof OAuth2AuthenticationToken) {
                redirectAttributes.addFlashAttribute("passwordError", "Password cannot be changed for social login accounts");
                return "redirect:/profile";
            }
            
            // Get current user
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                throw new IllegalStateException("User not found");
            }
            
            User user = userOpt.get();
            
            // Check if user uses OAuth (password change not allowed)
            if (user.getOauthProviders() != null && !user.getOauthProviders().isEmpty()) {
                redirectAttributes.addFlashAttribute("passwordError", "Password cannot be changed for social login accounts");
                return "redirect:/profile";
            }
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("passwordError", "Current password is incorrect");
                return "redirect:/profile";
            }
            
            // Verify new passwords match
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("passwordError", "New passwords do not match");
                return "redirect:/profile";
            }
            
            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.saveUser(user);
            
            redirectAttributes.addFlashAttribute("passwordSuccess", "Password updated successfully");
            return "redirect:/profile";
        } catch (Exception e) {
            logger.error("Error updating password", e);
            redirectAttributes.addFlashAttribute("passwordError", "Failed to update password: " + e.getMessage());
            return "redirect:/profile";
        }
    }
    
    // Delete user account
    @PostMapping("/delete-account")
    public String deleteAccount(Principal principal, RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/profile/edit";
        }
        
        User user = userOpt.get();
        String userId = user.getId();
        
        try {
            // Delete user's posts
            List<Post> userPosts = postService.findByUserId(userId);
            for (Post post : userPosts) {
                postService.deletePost(post.getId(), userId);
            }
            
            // Delete user's learning plans
            List<LearningPlan> userPlans = learningPlanService.getLearningPlansByUserId(userId);
            for (LearningPlan plan : userPlans) {
                learningPlanService.deleteLearningPlan(plan.getId(), userId);
            }
            
            // Delete user's notifications
            notificationService.deleteAllNotificationsForUser(userId);
            
            // Delete user (will also delete follows due to cascade)
            userService.deleteUser(userId);
            
            // Proper logout procedure
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }
            
            // Clear security context
            SecurityContextHolder.clearContext();
            
            // Invalidate session
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            return "redirect:/login?deleted=true";
        } catch (Exception e) {
            logger.error("Error deleting user account", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting your account");
            return "redirect:/profile/edit";
        }
    }

    @PostMapping("/upload-picture")
    public String uploadProfilePicture(@RequestParam("profilePicture") MultipartFile file, 
                                     RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Get current user
            Optional<User> userOpt = userService.findByEmail(email);
            User user;
            
            if (userOpt.isEmpty() && authentication instanceof OAuth2AuthenticationToken) {
                // Create new user for OAuth2 user
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                OAuth2User oAuth2User = oauthToken.getPrincipal();
                String registrationId = oauthToken.getAuthorizedClientRegistrationId();
                
                user = new User();
                user.setEmail(email);
                user.setEnabled(true);
                
                // Set OAuth provider
                user.setOauthProviders(new HashSet<>());
                user.addOAuthProvider(registrationId);
                
                // Add ROLE_USER
                user.setAuthorities(new HashSet<>());
                user.addAuthority("ROLE_USER");
                
                // Extract name from attributes
                Map<String, Object> attributes = oAuth2User.getAttributes();
                if (attributes.containsKey("given_name")) {
                    user.setFirstName((String) attributes.get("given_name"));
                }
                
                if (attributes.containsKey("family_name")) {
                    user.setLastName((String) attributes.get("family_name"));
                }
                
                // Default date of birth
                user.setDateOfBirth(LocalDate.now().minusYears(20));
            } else if (userOpt.isEmpty()) {
                throw new IllegalStateException("User not found");
            } else {
                user = userOpt.get();
            }
            
            // Validate file
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/profile";
            }
            
            // Check file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "File size should not exceed 5MB");
                return "redirect:/profile";
            }
            
            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Only image files are allowed");
                return "redirect:/profile";
            }
            
            // Convert to Base64 and store
            byte[] bytes = file.getBytes();
            String base64Image = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
            user.setProfilePicture(base64Image);
            
            // Save updated user
            userService.saveUser(user);
            
            redirectAttributes.addFlashAttribute("success", "Profile picture updated successfully");
            return "redirect:/profile";
        } catch (IOException e) {
            logger.error("Error uploading profile picture", e);
            redirectAttributes.addFlashAttribute("error", "Failed to upload profile picture: " + e.getMessage());
            return "redirect:/profile";
        }
    }

    @GetMapping("/posts")
    public ResponseEntity<?> getUserPosts() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Find the user
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            User user = userOptional.get();
            String userId = user.getId();
            
            // Get pageable for first page
            Pageable pageable = PageRequest.of(0, 10);
            
            // Get posts
            Page<PostResponse> posts = postService.getPostsByUserId(userId, userId, pageable);
            
            return ResponseEntity.ok(posts.getContent());
        } catch (Exception e) {
            logger.error("Error fetching user posts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Error fetching user posts: " + e.getMessage());
        }
    }

    // Post Management Endpoints
    
    @PostMapping("/posts/{postId}/delete")
    public String deletePost(@PathVariable String postId, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/profile";
        }
        
        User user = userOpt.get();
        
        try {
            boolean deleted = postService.deletePost(postId, user.getId());
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Post deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this post");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete post: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    @GetMapping("/posts/{postId}/edit")
    public String editPostForm(@PathVariable String postId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/profile";
        }
        
        User user = userOpt.get();
        
        Optional<Post> postOpt = postService.getPostById(postId);
        if (postOpt.isEmpty()) {
            return "redirect:/profile";
        }
        
        Post post = postOpt.get();
        
        // Check if user is the owner of the post
        if (!post.getUserId().equals(user.getId())) {
            return "redirect:/profile";
        }
        
        model.addAttribute("post", post);
        PostRequest postRequest = new PostRequest();
        postRequest.setDescription(post.getDescription());
        model.addAttribute("postRequest", postRequest);
        
        return "profile/edit-post";
    }
    
    @PostMapping("/posts/{postId}/update")
    public String updatePost(@PathVariable String postId, 
                            @ModelAttribute PostRequest postRequest,
                            @RequestParam(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles,
                            @RequestParam(value = "deleteMediaIds", required = false) List<String> deleteMediaIds,
                            Principal principal, 
                            RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/profile";
        }
        
        User user = userOpt.get();
        
        try {
            // First get the post to check ownership
            Optional<Post> postOpt = postService.getPostById(postId);
            if (postOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Post not found");
                return "redirect:/profile";
            }
            
            Post post = postOpt.get();
            
            // Check if user is the owner of the post
            if (!post.getUserId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this post");
                return "redirect:/profile";
            }
            
            // Update post description
            post = postService.updatePost(postId, user.getId(), postRequest);
            
            // Delete media if requested
            if (deleteMediaIds != null && !deleteMediaIds.isEmpty()) {
                for (String mediaId : deleteMediaIds) {
                    postService.deleteMediaFromPost(postId, mediaId, user.getId());
                }
            }
            
            // Add new media if provided
            if (mediaFiles != null && !mediaFiles.isEmpty()) {
                for (MultipartFile mediaFile : mediaFiles) {
                    if (!mediaFile.isEmpty()) {
                        try {
                            postService.addMediaToPost(postId, user.getId(), mediaFile);
                        } catch (Exception e) {
                            // Continue with other files even if one fails
                            redirectAttributes.addFlashAttribute("warning", 
                                    "Some media files couldn't be uploaded: " + e.getMessage());
                        }
                    }
                }
            }
            
            redirectAttributes.addFlashAttribute("success", "Post updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update post: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    @PostMapping("/posts/{postId}/media/{mediaId}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMedia(@PathVariable String postId, 
                                                         @PathVariable String mediaId, 
                                                         Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal == null) {
            response.put("success", false);
            response.put("error", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        Optional<User> userOpt = userService.findByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("error", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        User user = userOpt.get();
        
        try {
            postService.deleteMediaFromPost(postId, mediaId, user.getId());
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
} 