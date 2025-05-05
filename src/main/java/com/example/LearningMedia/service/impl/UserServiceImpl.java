package com.example.LearningMedia.service.impl;

import com.example.LearningMedia.dto.UserRegistrationDto;
import com.example.LearningMedia.model.User;
import com.example.LearningMedia.model.UserFollow;
import com.example.LearningMedia.repository.UserFollowRepository;
import com.example.LearningMedia.repository.UserRepository;
import com.example.LearningMedia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserServiceImpl implements UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserFollowRepository userFollowRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userFollowRepository = userFollowRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public User registerNewUser(UserRegistrationDto registrationDto) {
        // Validate if user is at least 15 years old
        if (!registrationDto.isAtLeast15YearsOld()) {
            throw new IllegalArgumentException("User must be at least 15 years old");
        }
        
        // Validate if email already exists
        if (existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Generate username from email if not provided
        String username = registrationDto.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = generateUniqueUsername(registrationDto.getEmail().split("@")[0]);
        } else if (existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Create new user
        User user = new User(
                registrationDto.getFirstName(),
                registrationDto.getLastName(),
                registrationDto.getDateOfBirth(),
                registrationDto.getEmail(),
                passwordEncoder.encode(registrationDto.getPassword())
        );
        
        user.setUsername(username);
        
        // Add default role
        user.addAuthority("ROLE_USER");
        
        // Save user
        logger.info("Registering new user with email: {}", registrationDto.getEmail());
        return userRepository.save(user);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    public User processOAuthPostLogin(String email, String name, String provider) {
        // Check if user exists
        Optional<User> existingUser = findByEmail(email);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            
            // Add OAuth provider if not already added
            user.addOAuthProvider(provider);
            
            // Ensure the user has a username if it was null (for older users)
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                String username = generateUniqueUsername(email.split("@")[0]);
                user.setUsername(username);
            }
            
            logger.info("Updated existing user {} with OAuth provider: {}", email, provider);
            
            return userRepository.save(user);
        } else {
            // Create new user with OAuth
            User newUser = new User();
            
            // Parse name to first and last name
            String[] names = name.split(" ");
            if (names.length > 0) {
                newUser.setFirstName(names[0]);
                
                if (names.length > 1) {
                    // Join remaining parts as last name
                    newUser.setLastName(String.join(" ", Arrays.copyOfRange(names, 1, names.length)));
                } else {
                    newUser.setLastName("");
                }
            } else {
                // Default names if none provided
                newUser.setFirstName("User");
                newUser.setLastName("");
            }
            
            newUser.setEmail(email);
            
            // Generate username from email
            String username = generateUniqueUsername(email.split("@")[0]);
            newUser.setUsername(username);
            
            newUser.setDateOfBirth(LocalDate.now().minusYears(20)); // Default age, will need to be updated
            newUser.setEnabled(true);
            newUser.addAuthority("ROLE_USER");
            newUser.addOAuthProvider(provider);
            
            // Initialize other required fields
            newUser.setSkills(new ArrayList<>());
            
            logger.info("Created new user {} from OAuth provider: {}", email, provider);
            return userRepository.save(newUser);
        }
    }
    
    @Override
    public User saveUser(User user) {
        logger.info("Saving user with email: {}", user.getEmail());
        return userRepository.save(user);
    }
    
    @Override
    public User updateProfile(String userId, User updatedUser) {
        Optional<User> existingUserOpt = userRepository.findById(userId);
        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        User existingUser = existingUserOpt.get();
        
        // Check if username is being updated
        if (updatedUser.getUsername() != null && !updatedUser.getUsername().equals(existingUser.getUsername())) {
            if (existsByUsername(updatedUser.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
            existingUser.setUsername(updatedUser.getUsername());
        }
        
        // Update only fields that can be changed in profile
        if (updatedUser.getBio() != null) {
            existingUser.setBio(updatedUser.getBio());
        }
        
        if (updatedUser.getProfilePicture() != null) {
            existingUser.setProfilePicture(updatedUser.getProfilePicture());
        }
        
        if (updatedUser.getSkills() != null && !updatedUser.getSkills().isEmpty()) {
            existingUser.setSkills(updatedUser.getSkills());
        }
        
        return userRepository.save(existingUser);
    }
    
    @Override
    @Transactional
    public boolean followUser(String followerId, String followingId) {
        // Check if users exist
        if (!userRepository.existsById(followerId) || !userRepository.existsById(followingId)) {
            return false;
        }
        
        // Prevent self-following
        if (followerId.equals(followingId)) {
            return false;
        }
        
        // Check if already following
        if (userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            return true; // Already following
        }
        
        // Create follow relationship
        UserFollow userFollow = new UserFollow(followerId, followingId);
        userFollowRepository.save(userFollow);
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean unfollowUser(String followerId, String followingId) {
        // Check if users exist
        if (!userRepository.existsById(followerId) || !userRepository.existsById(followingId)) {
            return false;
        }
        
        // Delete follow relationship
        userFollowRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
        
        return true;
    }
    
    @Override
    public boolean isFollowing(String followerId, String followingId) {
        return userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }
    
    @Override
    public List<User> getFollowers(String userId) {
        List<UserFollow> followers = userFollowRepository.findByFollowingId(userId);
        return followers.stream()
                .map(follow -> userRepository.findById(follow.getFollowerId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<User> getFollowing(String userId) {
        List<UserFollow> following = userFollowRepository.findByFollowerId(userId);
        return following.stream()
                .map(follow -> userRepository.findById(follow.getFollowingId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    @Override
    public long getFollowersCount(String userId) {
        return userFollowRepository.countByFollowingId(userId);
    }
    
    @Override
    public long getFollowingCount(String userId) {
        return userFollowRepository.countByFollowerId(userId);
    }
    
    // Helper method to generate a unique username
    private String generateUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;
        
        while (existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
    
    @Override
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return userRepository.findAll();
        }
        
        String searchTerm = query.toLowerCase().trim();
        
        return userRepository.findAll().stream()
            .filter(user -> 
                (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(searchTerm)) ||
                (user.getLastName() != null && user.getLastName().toLowerCase().contains(searchTerm)) ||
                (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchTerm)))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<User> getAllUsersExceptCurrent(String currentUserId) {
        return userRepository.findAll().stream()
            .filter(user -> !user.getId().equals(currentUserId))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public boolean deleteUser(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        // Delete all followers and following relationships
        userFollowRepository.deleteByFollowerId(userId);
        userFollowRepository.deleteByFollowingId(userId);
        
        // Delete the user
        userRepository.deleteById(userId);
        
        logger.info("Deleted user with ID: {}", userId);
        return true;
    }
} 