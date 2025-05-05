package com.example.LearningMedia.service.impl;

import com.example.LearningMedia.dto.LearningPlanDto;
import com.example.LearningMedia.model.LearningPlan;
import com.example.LearningMedia.model.User;
import com.example.LearningMedia.repository.LearningPlanRepository;
import com.example.LearningMedia.repository.UserRepository;
import com.example.LearningMedia.service.LearningPlanService;
import com.example.LearningMedia.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LearningPlanServiceImpl implements LearningPlanService {
    
    private static final Logger logger = LoggerFactory.getLogger(LearningPlanServiceImpl.class);
    
    private final LearningPlanRepository learningPlanRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    @Autowired
    public LearningPlanServiceImpl(LearningPlanRepository learningPlanRepository, 
                                  UserRepository userRepository,
                                  NotificationService notificationService) {
        this.learningPlanRepository = learningPlanRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }
    
    @Override
    public LearningPlan createLearningPlan(LearningPlanDto learningPlanDto, String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        User user = userOptional.get();
        
        LearningPlan learningPlan = new LearningPlan(userId, learningPlanDto.getTitle(), learningPlanDto.getDeadline());
        learningPlan.setUserFirstName(user.getFirstName());
        learningPlan.setUserLastName(user.getLastName());
        
        // Convert topics from DTO to entity
        List<LearningPlan.Topic> topics = learningPlanDto.getTopics().stream()
                .map(topicDto -> {
                    LearningPlan.Topic topic = new LearningPlan.Topic(topicDto.getName());
                    if (topicDto.getId() != null) {
                        topic.setId(topicDto.getId());
                    }
                    topic.setResourceUrls(topicDto.getResourceUrls());
                    topic.setCompleted(topicDto.isCompleted());
                    topic.setCompletedAt(topicDto.getCompletedAt());
                    topic.setEstimatedMinutes(topicDto.getEstimatedMinutes());
                    return topic;
                })
                .collect(Collectors.toList());
        
        learningPlan.setTopics(topics);
        
        // Calculate initial progress
        learningPlan.updateProgress();
        
        return learningPlanRepository.save(learningPlan);
    }
    
    @Override
    public List<LearningPlan> getLearningPlansByUserId(String userId) {
        return learningPlanRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    @Override
    public List<LearningPlan> getLearningPlansWithProgressByUserId(String userId) {
        // Since progress is now a part of the LearningPlan model, we can reuse this method
        return getLearningPlansByUserId(userId);
    }
    
    @Override
    public Optional<LearningPlan> getLearningPlanById(String id, String userId) {
        Optional<LearningPlan> planOptional = learningPlanRepository.findById(id);
        
        if (planOptional.isPresent() && planOptional.get().getUserId().equals(userId)) {
            return planOptional;
        }
        
        return Optional.empty();
    }
    
    @Override
    public LearningPlan updateLearningPlan(String id, LearningPlanDto learningPlanDto, String userId) {
        Optional<LearningPlan> existingPlanOptional = getLearningPlanById(id, userId);
        
        if (existingPlanOptional.isEmpty()) {
            throw new IllegalArgumentException("Learning plan not found or you don't have permission to update it");
        }
        
        LearningPlan existingPlan = existingPlanOptional.get();
        existingPlan.setTitle(learningPlanDto.getTitle());
        existingPlan.setDeadline(learningPlanDto.getDeadline());
        
        // Update topics
        List<LearningPlan.Topic> updatedTopics = learningPlanDto.getTopics().stream()
                .map(topicDto -> {
                    LearningPlan.Topic topic;
                    
                    // If topic has an ID and exists in current plan, update it
                    if (topicDto.getId() != null) {
                        Optional<LearningPlan.Topic> existingTopic = existingPlan.getTopics().stream()
                                .filter(t -> t.getId().equals(topicDto.getId()))
                                .findFirst();
                        
                        if (existingTopic.isPresent()) {
                            topic = existingTopic.get();
                            topic.setName(topicDto.getName());
                            topic.setResourceUrls(topicDto.getResourceUrls());
                            topic.setCompleted(topicDto.isCompleted());
                            topic.setCompletedAt(topicDto.getCompletedAt());
                            topic.setEstimatedMinutes(topicDto.getEstimatedMinutes());
                            return topic;
                        }
                    }
                    
                    // If no existing topic found or no ID provided, create new topic
                    topic = new LearningPlan.Topic(topicDto.getName());
                    topic.setResourceUrls(topicDto.getResourceUrls());
                    topic.setCompleted(topicDto.isCompleted());
                    topic.setCompletedAt(topicDto.getCompletedAt());
                    topic.setEstimatedMinutes(topicDto.getEstimatedMinutes());
                    return topic;
                })
                .collect(Collectors.toList());
        
        existingPlan.setTopics(updatedTopics);
        existingPlan.setUpdatedAt(LocalDateTime.now());
        
        // Recalculate progress
        existingPlan.updateProgress();
        
        return learningPlanRepository.save(existingPlan);
    }
    
    @Override
    public LearningPlan updateTopicCompletionStatus(String planId, String topicId, boolean completed, String userId) {
        LearningPlan plan = getLearningPlanById(planId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Learning plan not found or access denied"));
        
        // Ensure the user owns this plan
        if (!plan.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You can only update your own learning plans");
        }
        
        boolean wasUpdated = plan.updateTopicCompletionStatus(topicId, completed);
        
        if (!wasUpdated) {
            throw new IllegalArgumentException("Topic not found in the learning plan");
        }
        
        plan.setUpdatedAt(LocalDateTime.now());
        LearningPlan updatedPlan = learningPlanRepository.save(plan);
        
        // Check if the learning plan is now completed and create a notification if it is
        if (completed && updatedPlan.isCompleted()) {
            notificationService.createLearningPlanCompletedNotification(planId, userId);
        }
        
        return updatedPlan;
    }
    
    @Override
    @Transactional
    public boolean deleteLearningPlan(String id, String userId) {
        if (learningPlanRepository.existsByIdAndUserId(id, userId)) {
            learningPlanRepository.deleteByIdAndUserId(id, userId);
            return true;
        }
        return false;
    }
    
    @Override
    public LearningPlan updatePlanSharingStatus(String planId, String userId, boolean shared) {
        Optional<LearningPlan> planOptional = getLearningPlanById(planId, userId);
        
        if (planOptional.isEmpty()) {
            throw new IllegalArgumentException("Learning plan not found or you don't have permission to update it");
        }
        
        LearningPlan plan = planOptional.get();
        plan.setShared(shared);
        plan.setUpdatedAt(LocalDateTime.now());
        
        return learningPlanRepository.save(plan);
    }
    
    @Override
    public LearningPlan updatePlanSharingStatusAndDescription(String planId, String userId, boolean shared, String description) {
        Optional<LearningPlan> planOptional = getLearningPlanById(planId, userId);
        
        if (planOptional.isEmpty()) {
            throw new IllegalArgumentException("Learning plan not found or you don't have permission to update it");
        }
        
        LearningPlan plan = planOptional.get();
        plan.setShared(shared);
        
        // Update description if provided
        if (description != null && !description.isEmpty()) {
            plan.setDescription(description);
        }
        
        plan.setUpdatedAt(LocalDateTime.now());
        
        return learningPlanRepository.save(plan);
    }
    
    @Override
    public List<LearningPlan> getSharedLearningPlansByUserId(String userId) {
        return learningPlanRepository.findByUserIdAndIsSharedOrderByCreatedAtDesc(userId, true);
    }
    
    @Override
    public LearningPlan addSharedPlanToMyPlans(String planId, String currentUserId) {
        Optional<LearningPlan> sharedPlanOptional = learningPlanRepository.findById(planId);
        
        if (sharedPlanOptional.isEmpty() || !sharedPlanOptional.get().isShared()) {
            throw new IllegalArgumentException("Shared learning plan not found");
        }
        
        // Don't allow adding your own plan
        LearningPlan sharedPlan = sharedPlanOptional.get();
        if (sharedPlan.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("You cannot add your own plan to your plans");
        }
        
        // Check if user already has a copy of this plan (based on title with "(Copied)" suffix)
        String copyTitle = sharedPlan.getTitle() + " (Copied)";
        List<LearningPlan> userPlans = learningPlanRepository.findByUserId(currentUserId);
        boolean alreadyAdded = userPlans.stream()
                .anyMatch(plan -> plan.getTitle().equals(copyTitle));
        
        if (alreadyAdded) {
            throw new IllegalArgumentException("You have already added this plan to your collection");
        }
        
        // Get user info
        Optional<User> currentUserOptional = userRepository.findById(currentUserId);
        if (currentUserOptional.isEmpty()) {
            throw new IllegalArgumentException("Current user not found");
        }
        
        User currentUser = currentUserOptional.get();
        
        // Create a new plan based on the shared one
        LearningPlan newPlan = new LearningPlan();
        newPlan.setUserId(currentUserId);
        newPlan.setUserFirstName(currentUser.getFirstName());
        newPlan.setUserLastName(currentUser.getLastName());
        newPlan.setTitle(copyTitle);
        newPlan.setDeadline(sharedPlan.getDeadline());
        newPlan.setShared(false); // New copy is not shared by default
        newPlan.setDescription(sharedPlan.getDescription());
        
        // Copy topics
        List<LearningPlan.Topic> copiedTopics = sharedPlan.getTopics().stream()
                .map(topic -> {
                    LearningPlan.Topic newTopic = new LearningPlan.Topic(topic.getName());
                    newTopic.setResourceUrls(new ArrayList<>(topic.getResourceUrls()));
                    newTopic.setEstimatedMinutes(topic.getEstimatedMinutes());
                    // Reset completion status for new owner
                    newTopic.setCompleted(false);
                    newTopic.setCompletedAt(null);
                    return newTopic;
                })
                .collect(Collectors.toList());
        
        newPlan.setTopics(copiedTopics);
        newPlan.updateProgress();
        
        return learningPlanRepository.save(newPlan);
    }
    
    @Override
    public Optional<LearningPlan> getLearningPlanByIdForShared(String id) {
        return learningPlanRepository.findById(id);
    }
    
    @Override
    public List<LearningPlan> getAllSharedLearningPlans() {
        return learningPlanRepository.findByIsSharedOrderByCreatedAtDesc(true);
    }
} 