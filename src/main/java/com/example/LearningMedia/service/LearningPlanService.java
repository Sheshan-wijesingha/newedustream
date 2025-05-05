package com.example.LearningMedia.service;

import com.example.LearningMedia.dto.LearningPlanDto;
import com.example.LearningMedia.model.LearningPlan;

import java.util.List;
import java.util.Optional;

public interface LearningPlanService {
    
    LearningPlan createLearningPlan(LearningPlanDto learningPlanDto, String userId);
    
    List<LearningPlan> getLearningPlansByUserId(String userId);
    
    Optional<LearningPlan> getLearningPlanById(String id, String userId);
    
    LearningPlan updateLearningPlan(String id, LearningPlanDto learningPlanDto, String userId);
    
    boolean deleteLearningPlan(String id, String userId);
    
    /**
     * Updates the completion status of a topic in a learning plan
     *
     * @param planId the ID of the learning plan
     * @param topicId the ID of the topic to update
     * @param completed the new completion status
     * @param userId the ID of the user who owns the plan
     * @return the updated learning plan if successful
     * @throws IllegalArgumentException if the plan or topic is not found
     */
    LearningPlan updateTopicCompletionStatus(String planId, String topicId, boolean completed, String userId);
    
    /**
     * Retrieves learning plans by user ID with progress information
     *
     * @param userId the ID of the user
     * @return a list of learning plans with progress information
     */
    List<LearningPlan> getLearningPlansWithProgressByUserId(String userId);
    
    /**
     * Shares or unshares a learning plan
     *
     * @param planId the ID of the learning plan
     * @param userId the ID of the owner of the plan
     * @param shared the new sharing status
     * @return the updated learning plan
     * @throws IllegalArgumentException if the plan is not found or user doesn't own it
     */
    LearningPlan updatePlanSharingStatus(String planId, String userId, boolean shared);
    
    /**
     * Shares or unshares a learning plan and updates its description
     *
     * @param planId the ID of the learning plan
     * @param userId the ID of the owner of the plan
     * @param shared the new sharing status
     * @param description optional description for the shared plan
     * @return the updated learning plan
     * @throws IllegalArgumentException if the plan is not found or user doesn't own it
     */
    LearningPlan updatePlanSharingStatusAndDescription(String planId, String userId, boolean shared, String description);
    
    /**
     * Retrieves all shared learning plans by a user
     *
     * @param userId the ID of the user
     * @return a list of shared learning plans
     */
    List<LearningPlan> getSharedLearningPlansByUserId(String userId);
    
    /**
     * Adds a shared learning plan to the current user's learning plans
     *
     * @param planId the ID of the shared learning plan
     * @param currentUserId the ID of the current user
     * @return the copied learning plan
     * @throws IllegalArgumentException if the plan is not found or not shared
     */
    LearningPlan addSharedPlanToMyPlans(String planId, String currentUserId);
    
    /**
     * Retrieves a learning plan by ID, regardless of ownership.
     * This is used for fetching shared plans.
     *
     * @param id the ID of the learning plan
     * @return an optional containing the learning plan if found
     */
    Optional<LearningPlan> getLearningPlanByIdForShared(String id);
    
    /**
     * Retrieves all shared learning plans across the platform
     *
     * @return a list of all shared learning plans
     */
    List<LearningPlan> getAllSharedLearningPlans();
} 