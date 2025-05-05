package com.example.LearningMedia.service;

import com.example.LearningMedia.dto.UserRegistrationDto;
import com.example.LearningMedia.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    
    User registerNewUser(UserRegistrationDto registrationDto);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findById(String id);
    
    Optional<User> findByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    User processOAuthPostLogin(String email, String name, String provider);
    
    User saveUser(User user);
    
    User updateProfile(String userId, User updatedUser);
    
    boolean followUser(String followerId, String followingId);
    
    boolean unfollowUser(String followerId, String followingId);
    
    boolean isFollowing(String followerId, String followingId);
    
    List<User> getFollowers(String userId);
    
    List<User> getFollowing(String userId);
    
    long getFollowersCount(String userId);
    
    long getFollowingCount(String userId);
    
    /**
     * Search for users by name or username
     * 
     * @param query The search query to match against first name, last name, or username
     * @return A list of users matching the search criteria
     */
    List<User> searchUsers(String query);
    
    /**
     * Get all users in the system except the current user
     *
     * @param currentUserId The ID of the current user to exclude
     * @return A list of all users except the current user
     */
    List<User> getAllUsersExceptCurrent(String currentUserId);
    
    /**
     * Delete a user account and related data
     * 
     * @param userId The ID of the user to delete
     * @return true if the user was successfully deleted
     */
    boolean deleteUser(String userId);
} 