package com.example.LearningMedia.repository;

import com.example.LearningMedia.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    /**
     * 
     * 
     * @param userId The ID of the user whose followed users we want to find
     * @return A list of user IDs that the specified user is following
     */
    @Query(value = "{ 'followerId': ?0 }", fields = "{ 'followingId': 1 }")
    List<String> findFollowedUserIds(String userId);
} 