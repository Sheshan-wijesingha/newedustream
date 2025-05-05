package com.example.LearningMedia.controller;

import com.example.LearningMedia.model.LearningPlan;
import com.example.LearningMedia.model.User;
import com.example.LearningMedia.service.LearningPlanService;
import com.example.LearningMedia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.LearningMedia.service.NotificationService;

@Controller
public class ViewController {
    
    private final UserService userService;
    private final LearningPlanService learningPlanService;
    private final NotificationService notificationService;
    
    @Autowired
    public ViewController(UserService userService, LearningPlanService learningPlanService,
                        NotificationService notificationService) {
        this.userService = userService;
        this.learningPlanService = learningPlanService;
        this.notificationService = notificationService;
    }
    
    // Helper method to add common attributes to model for all views with navigation
    private void addCommonAttributes(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("currentUser", user);
                
                // Add notification count
                long unreadNotificationCount = notificationService.countUnreadNotifications(user.getId());
                model.addAttribute("unreadNotificationCount", unreadNotificationCount);
            }
        }
    }
    
    @GetMapping("/")
    public String index(Model model, Authentication auth) {
        addCommonAttributes(model, auth);
        return "index";
    }
    
    @GetMapping("/login")
    public String login(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            addCommonAttributes(model, auth);
            return "redirect:/dashboard";
        }
        return "login";
    }
    
    @GetMapping("/register")
    public String register(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            addCommonAttributes(model, auth);
        }
        return "register";
    }
    
    @GetMapping("/feed")
    public String feed(Model model, Authentication auth) {
        addCommonAttributes(model, auth);
        
        if (auth == null || !auth.isAuthenticated() || 
                auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        return "feed";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        addCommonAttributes(model, auth);
        
        if (auth == null || !auth.isAuthenticated() || 
                auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        String email = auth.getName();
        User user = null;
        
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else if (auth instanceof OAuth2AuthenticationToken) {
            // Handle OAuth2 authentication
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            
            // Create a basic user object for display purposes
            user = new User();
            user.setEmail(email);
            
            // Get name from OAuth2 attributes
            Map<String, Object> attributes = oAuth2User.getAttributes();
            
            if (attributes.containsKey("given_name")) {
                user.setFirstName((String) attributes.get("given_name"));
            } else if (attributes.containsKey("name")) {
                String name = (String) attributes.get("name");
                String[] parts = name.split(" ");
                user.setFirstName(parts[0]);
                if (parts.length > 1) {
                    user.setLastName(parts[parts.length - 1]);
                }
            }
            
            if (attributes.containsKey("family_name")) {
                user.setLastName((String) attributes.get("family_name"));
            }
            
            if (attributes.containsKey("picture")) {
                user.setProfilePicture((String) attributes.get("picture"));
            }
        }
        
        // If user is still null, create a default user to avoid template errors
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFirstName("User");
            user.setLastName("");
        }
        
        model.addAttribute("user", user);
        
        return "dashboard";
    }
    
    // Learning plans page
    @GetMapping("/learning-plans")
    public String learningPlans(Model model, Authentication auth) {
        addCommonAttributes(model, auth);
        
        if (auth == null || !auth.isAuthenticated() || 
                auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        String email = auth.getName();
        User user = null;
        
        if (auth != null && auth.isAuthenticated()) {
            if (auth instanceof OAuth2AuthenticationToken) {
                // Handle OAuth2 authentication
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
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
                        email = auth.getName();
                    }
                }
                
                // Create a basic user object for display purposes if we don't find the user in the database
                Optional<User> userOpt = userService.findByEmail(email);
                if (userOpt.isPresent()) {
                    user = userOpt.get();
                } else {
                    user = new User();
                    user.setEmail(email);
                    
                    // Get name from OAuth2 attributes
                    Map<String, Object> attributes = oauth2User.getAttributes();
                    
                    if (attributes.containsKey("given_name")) {
                        user.setFirstName((String) attributes.get("given_name"));
                    } else if (attributes.containsKey("name")) {
                        String name = (String) attributes.get("name");
                        String[] parts = name.split(" ");
                        user.setFirstName(parts[0]);
                        if (parts.length > 1) {
                            user.setLastName(parts[parts.length - 1]);
                        }
                    }
                    
                    if (attributes.containsKey("family_name")) {
                        user.setLastName((String) attributes.get("family_name"));
                    }
                    
                    if (attributes.containsKey("picture")) {
                        user.setProfilePicture((String) attributes.get("picture"));
                    }
                }
            } else {
                // Handle username/password authentication
                email = auth.getName();
                Optional<User> userOpt = userService.findByEmail(email);
                if (userOpt.isPresent()) {
                    user = userOpt.get();
                }
            }
        }
        
        // If user is still null, create a default user to avoid template errors
        if (user == null) {
            user = new User();
            user.setEmail(email != null ? email : "anonymous");
            user.setFirstName("User");
            user.setLastName("");
        }
        
        // Add user to model
        model.addAttribute("user", user);
        
        // Get learning plans for this user if they have an ID
        if (user.getId() != null) {
            List<LearningPlan> userPlans = learningPlanService.getLearningPlansWithProgressByUserId(user.getId());
            model.addAttribute("learningPlans", userPlans);
        } else {
            model.addAttribute("learningPlans", new ArrayList<>());
        }
        
        return "learning-plans";
    }
    
    // Explore learning plans page
    @GetMapping("/explore")
    public String exploreLearningPlans(Model model, Authentication auth) {
        addCommonAttributes(model, auth);
        
        if (auth == null || !auth.isAuthenticated() || 
                auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        String email = auth.getName();
        User user = null;
        
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else if (auth instanceof OAuth2AuthenticationToken) {
            // Handle OAuth2 authentication
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            
            // Create a basic user object for display purposes
            user = new User();
            user.setEmail(email);
            
            // Get name from OAuth2 attributes
            Map<String, Object> attributes = oAuth2User.getAttributes();
            
            if (attributes.containsKey("given_name")) {
                user.setFirstName((String) attributes.get("given_name"));
            } else if (attributes.containsKey("name")) {
                String name = (String) attributes.get("name");
                String[] parts = name.split(" ");
                user.setFirstName(parts[0]);
                if (parts.length > 1) {
                    user.setLastName(parts[parts.length - 1]);
                }
            }
            
            if (attributes.containsKey("family_name")) {
                user.setLastName((String) attributes.get("family_name"));
            }
            
            if (attributes.containsKey("picture")) {
                user.setProfilePicture((String) attributes.get("picture"));
            }
        }
        
        // If user is still null, create a default user to avoid template errors
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFirstName("User");
            user.setLastName("");
        }
        
        // Get all shared learning plans
        List<LearningPlan> sharedPlans = learningPlanService.getAllSharedLearningPlans();
        
        // If user is logged in, check which plans they own or have added already
        if (user.getId() != null) {
            List<String> userPlansIds = learningPlanService.getLearningPlansByUserId(user.getId())
                .stream()
                .map(LearningPlan::getId)
                .collect(Collectors.toList());
            
            // Create a final copy of the user object to use in the lambda
            final User finalUser = user;
            
            // Add a flag to each plan indicating if the user already has it
            sharedPlans.forEach(plan -> {
                plan.setUserOwnsIt(plan.getUserId().equals(finalUser.getId()) || userPlansIds.contains(plan.getId()));
            });
        }
        
        model.addAttribute("user", user);
        model.addAttribute("sharedPlans", sharedPlans);
        
        return "explore-learning-plans";
    }
    
    // Create learning plan page
    @GetMapping("/create-learning-plan")
    public String createLearningPlan(Model model, Authentication auth) {
        addCommonAttributes(model, auth);
        
        if (auth == null || !auth.isAuthenticated() || 
                auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        String email = auth.getName();
        User user = null;
        
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else if (auth instanceof OAuth2AuthenticationToken) {
            // Handle OAuth2 authentication
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            
            // Create a basic user object for display purposes
            user = new User();
            user.setEmail(email);
            
            // Get name from OAuth2 attributes
            Map<String, Object> attributes = oAuth2User.getAttributes();
            
            if (attributes.containsKey("given_name")) {
                user.setFirstName((String) attributes.get("given_name"));
            } else if (attributes.containsKey("name")) {
                String name = (String) attributes.get("name");
                String[] parts = name.split(" ");
                user.setFirstName(parts[0]);
                if (parts.length > 1) {
                    user.setLastName(parts[parts.length - 1]);
                }
            }
            
            if (attributes.containsKey("family_name")) {
                user.setLastName((String) attributes.get("family_name"));
            }
            
            if (attributes.containsKey("picture")) {
                user.setProfilePicture((String) attributes.get("picture"));
            }
        }
        
        // If user is still null, create a default user to avoid template errors
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFirstName("User");
            user.setLastName("");
        }
        
        model.addAttribute("user", user);
        
        return "create-learning-plan";
    }

    // View learning plan detail
    @GetMapping("/learning-plan/{id}")
    public String viewLearningPlan(@PathVariable String id, Model model, Authentication auth) {
        addCommonAttributes(model, auth);
        
        if (auth == null || !auth.isAuthenticated() || 
                auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        String email = auth.getName();
        User user = null;
        
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else if (auth instanceof OAuth2AuthenticationToken) {
            // Handle OAuth2 authentication similar to other methods
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            
            // Create a basic user object for display purposes
            user = new User();
            user.setEmail(email);
            
            // Get name from OAuth2 attributes
            Map<String, Object> attributes = oAuth2User.getAttributes();
            
            if (attributes.containsKey("given_name")) {
                user.setFirstName((String) attributes.get("given_name"));
            } else if (attributes.containsKey("name")) {
                String name = (String) attributes.get("name");
                String[] parts = name.split(" ");
                user.setFirstName(parts[0]);
                if (parts.length > 1) {
                    user.setLastName(parts[parts.length - 1]);
                }
            }
            
            if (attributes.containsKey("family_name")) {
                user.setLastName((String) attributes.get("family_name"));
            }
            
            if (attributes.containsKey("picture")) {
                user.setProfilePicture((String) attributes.get("picture"));
            }
        }
        
        // If user is still null, create a default user to avoid template errors
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFirstName("User");
            user.setLastName("");
        }
        
        // Check if the user can access this learning plan
        boolean isOwner = false;
        boolean isShared = false;
        
        // Check if user owns the plan or if it's a shared plan
        Optional<LearningPlan> planOptional = Optional.empty();
        if (user.getId() != null) {
            planOptional = learningPlanService.getLearningPlanById(id, user.getId());
        }
        
        if (planOptional.isEmpty()) {
            // If not owner, check if it's a shared plan
            planOptional = learningPlanService.getLearningPlanByIdForShared(id)
                .filter(LearningPlan::isShared);
            
            if (planOptional.isPresent()) {
                isShared = true;
            } else {
                // If neither owner nor shared, return access denied
                return "error/access-denied";
            }
        } else {
            isOwner = true;
        }
        
        model.addAttribute("user", user);
        model.addAttribute("planId", id);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isShared", isShared);
        model.addAttribute("plan", planOptional.get());
        
        return "learning-plan-detail";
    }

    // Add a notifications page
    @GetMapping("/notifications")
    public String showNotifications(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || 
                auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        addCommonAttributes(model, auth);
        
        return "notifications";
    }
} 