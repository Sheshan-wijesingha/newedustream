package com.example.LearningMedia.repository;

import com.example.LearningMedia.model.LearningPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningPlanRepository extends MongoRepository<LearningPlan, String> {
    
    List<LearningPlan> findByUserId(String userId);
    
    List<LearningPlan> findByUserIdOrderByCreatedAtDesc(String userId);
    
    boolean existsByIdAndUserId(String id, String userId);
    
    void deleteByIdAndUserId(String id, String userId);
    
    /**
     * Finds all shared learning plans for a specific user
     *
     * @param userId the ID of the user
     * @param isShared whether the plans are shared (true) or not
     * @return a list of shared learning plans
     */
    List<LearningPlan> findByUserIdAndIsSharedOrderByCreatedAtDesc(String userId, boolean isShared);
    
    /**
     * Finds all shared learning plans regardless of user
     *
     * @return a list of all shared learning plans
     */
    List<LearningPlan> findByIsSharedOrderByCreatedAtDesc(boolean isShared);
} 