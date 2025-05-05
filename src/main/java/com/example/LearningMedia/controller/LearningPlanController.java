package com.example.LearningMedia.controller;

import com.example.LearningMedia.dto.LearningPlanDto;
import com.example.LearningMedia.model.LearningPlan;
import com.example.LearningMedia.model.User;
import com.example.LearningMedia.service.LearningPlanService;
import com.example.LearningMedia.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/learning-plans")
public class LearningPlanController {
    
    private final LearningPlanService learningPlanService;
    private final UserService userService;
    
    @Autowired
    public LearningPlanController(LearningPlanService learningPlanService, UserService userService) {
        this.learningPlanService = learningPlanService;
        this.userService = userService;
    }
    
    @PostMapping
    public ResponseEntity<?> createLearningPlan(@Valid @RequestBody LearningPlanDto learningPlanDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = null;
            
            if (authentication instanceof OAuth2AuthenticationToken) {
                // Handle OAuth2 authentication
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                OAuth2User oauth2User = oauthToken.getPrincipal();
                
                // Different OAuth providers may store email in different attributes
                if (oauth2User.getAttribute("email") != null) {
                    email = oauth2User.getAttribute("email");
                } else if (oauth2User.getAttributes().containsKey("email")) {
                    email = (String) oauth2User.getAttributes().get("email");
                } else {
                    // For providers that might have a different structure
                    Map<String, Object> attributes = oauth2User.getAttributes();
                    if (attributes.containsKey("emailAddress")) {
                        email = (String) attributes.get("emailAddress");
                    } else if (attributes.containsKey("mail")) {
                        email = (String) attributes.get("mail");
                    } else {
                        // Last resort - try to get from authentication name
                        email = authentication.getName();
                    }
                }
            } else {
                // Handle username/password authentication
                email = authentication.getName();
            }
            
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
            }
            
            // Get the user by email
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
            }
            
            // Get the actual user ID
            String userId = userOptional.get().getId();
            
            LearningPlan createdPlan = learningPlanService.createLearningPlan(learningPlanDto, userId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPlan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<LearningPlan>> getLearningPlans() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = null;
            
        if (authentication instanceof OAuth2AuthenticationToken) {
            // Handle OAuth2 authentication
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauthToken.getPrincipal();
            
            // Different OAuth providers may store email in different attributes
            if (oauth2User.getAttribute("email") != null) {
                email = oauth2User.getAttribute("email");
            } else if (oauth2User.getAttributes().containsKey("email")) {
                email = (String) oauth2User.getAttributes().get("email");
            } else {
                // For providers that might have a different structure
                Map<String, Object> attributes = oauth2User.getAttributes();
                if (attributes.containsKey("emailAddress")) {
                    email = (String) attributes.get("emailAddress");
                } else if (attributes.containsKey("mail")) {
                    email = (String) attributes.get("mail");
                } else {
                    // Last resort - try to get from authentication name
                    email = authentication.getName();
                }
            }
        } else {
            // Handle username/password authentication
            email = authentication.getName();
        }
        
        if (email == null) {
            return ResponseEntity.ok(List.of()); // Return empty list if email not found
        }
        
        // Get the user by email
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.ok(List.of()); // Return empty list if user not found
        }
        
        // Get the actual user ID
        String userId = userOptional.get().getId();
        
        List<LearningPlan> plans = learningPlanService.getLearningPlansWithProgressByUserId(userId);
        
        return ResponseEntity.ok(plans);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getLearningPlan(@PathVariable("id") String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = null;
            
        if (authentication instanceof OAuth2AuthenticationToken) {
            // Handle OAuth2 authentication
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauthToken.getPrincipal();
            
            // Different OAuth providers may store email in different attributes
            if (oauth2User.getAttribute("email") != null) {
                email = oauth2User.getAttribute("email");
            } else if (oauth2User.getAttributes().containsKey("email")) {
                email = (String) oauth2User.getAttributes().get("email");
            } else {
                // For providers that might have a different structure
                Map<String, Object> attributes = oauth2User.getAttributes();
                if (attributes.containsKey("emailAddress")) {
                    email = (String) attributes.get("emailAddress");
                } else if (attributes.containsKey("mail")) {
                    email = (String) attributes.get("mail");
                } else {
                    // Last resort - try to get from authentication name
                    email = authentication.getName();
                }
            }
        } else {
            // Handle username/password authentication
            email = authentication.getName();
        }
        
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated"));
        }
        
        Optional<User> userOpt = userService.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not found"));
        }
        
        User currentUser = userOpt.get();
        Optional<LearningPlan> planOpt = learningPlanService.getLearningPlanById(id, currentUser.getId());
        if (planOpt.isEmpty()) {
            // If not owner, check if it's a shared plan
            planOpt = learningPlanService.getLearningPlanByIdForShared(id);
            if (planOpt.isEmpty() || !planOpt.get().isShared()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Learning plan not found"));
            }
        }
        
        LearningPlan plan = planOpt.get();
        
        // Allow access if the user owns the plan or if the plan is shared
        if (!plan.getUserId().equals(currentUser.getId()) && !plan.isShared()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You do not have permission to view this learning plan"));
        }
        
        return ResponseEntity.ok(plan);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLearningPlan(
            @PathVariable String id,
            @Valid @RequestBody LearningPlanDto learningPlanDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Get the user by email
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
            }
            
            // Get the actual user ID
            String userId = userOptional.get().getId();
            
            LearningPlan updatedPlan = learningPlanService.updateLearningPlan(id, learningPlanDto, userId);
            
            return ResponseEntity.ok(updatedPlan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Updates a topic's completion status within a learning plan
     */
    @PatchMapping("/{planId}/topics/{topicId}/completion")
    public ResponseEntity<?> updateTopicCompletionStatus(
            @PathVariable String planId,
            @PathVariable String topicId,
            @RequestParam boolean completed) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Get the user by email
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
            }
            
            // Get the actual user ID
            String userId = userOptional.get().getId();
            
            LearningPlan updatedPlan = learningPlanService.updateTopicCompletionStatus(planId, topicId, completed, userId);
            
            Map<String, Object> response = Map.of(
                    "message", "Topic completion status updated successfully",
                    "completed", completed,
                    "progressPercentage", updatedPlan.getProgressPercentage(),
                    "completedTopics", updatedPlan.getCompletedTopics(),
                    "totalTopics", updatedPlan.getTopics().size()
            );
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLearningPlan(@PathVariable String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        // Get the user by email
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Get the actual user ID
        String userId = userOptional.get().getId();
        
        boolean deleted = learningPlanService.deleteLearningPlan(id, userId);
        
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Learning plan deleted successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Updates the sharing status of a learning plan
     */
    @PatchMapping("/{id}/share")
    public ResponseEntity<?> updateSharingStatus(
            @PathVariable String id,
            @RequestParam boolean shared,
            @RequestParam(required = false) String description) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Get the user by email
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
            }
            
            // Get the actual user ID
            String userId = userOptional.get().getId();
            
            // Update both sharing status and description in one operation
            LearningPlan updatedPlan = learningPlanService.updatePlanSharingStatusAndDescription(id, userId, shared, description);
            
            String message = shared ? "Learning plan is now shared" : "Learning plan is no longer shared";
            return ResponseEntity.ok(Map.of(
                "message", message,
                "isShared", updatedPlan.isShared()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Adds a shared learning plan to the current user's plans
     */
    @PostMapping("/add/{id}")
    public ResponseEntity<Map<String, Object>> addSharedPlanToMyPlans(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = null;
        
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication instanceof OAuth2AuthenticationToken) {
                // Handle OAuth2 authentication
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                OAuth2User oauth2User = oauthToken.getPrincipal();
                
                // Different OAuth providers may store email in different attributes
                if (oauth2User.getAttribute("email") != null) {
                    email = oauth2User.getAttribute("email");
                } else if (oauth2User.getAttributes().containsKey("email")) {
                    email = (String) oauth2User.getAttributes().get("email");
                } else {
                    // For providers that might have a different structure
                    Map<String, Object> attributes = oauth2User.getAttributes();
                    if (attributes.containsKey("emailAddress")) {
                        email = (String) attributes.get("emailAddress");
                    } else if (attributes.containsKey("mail")) {
                        email = (String) attributes.get("mail");
                    } else {
                        // Last resort - try to get from authentication name
                        email = authentication.getName();
                    }
                }
            } else {
                // Handle username/password authentication
                email = authentication.getName();
            }
        }

        if (email == null) {
            response.put("success", false);
            response.put("error", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("error", "User not found with email: " + email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        try {
            LearningPlan addedPlan = learningPlanService.addSharedPlanToMyPlans(id, userOpt.get().getId());
            
            if (addedPlan != null) {
                response.put("success", true);
                response.put("message", "Learning plan added to your collection");
                response.put("plan", addedPlan);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "Unable to add the learning plan to your collection");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/shared")
    public ResponseEntity<?> getAllSharedLearningPlans() {
        List<LearningPlan> sharedPlans = learningPlanService.getAllSharedLearningPlans();
        
        // Get current user for determining if user has already added these plans
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<User> userOpt = userService.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User currentUser = userOpt.get();
            List<String> userPlansIds = learningPlanService.getLearningPlansByUserId(currentUser.getId())
                .stream()
                .map(LearningPlan::getId)
                .collect(Collectors.toList());
            
            // Add a flag to each plan indicating if the user already has it
            List<Map<String, Object>> enrichedPlans = sharedPlans.stream()
                .map(plan -> {
                    Map<String, Object> enrichedPlan = new HashMap<>();
                    enrichedPlan.put("plan", plan);
                    enrichedPlan.put("userOwnsIt", plan.getUserId().equals(currentUser.getId()) || userPlansIds.contains(plan.getId()));
                    return enrichedPlan;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(enrichedPlans);
        }
        
        // If user is not logged in or not found, just return the plans
        return ResponseEntity.ok(sharedPlans);
    }
} 