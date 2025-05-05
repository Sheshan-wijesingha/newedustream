package com.example.LearningMedia.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "learning_plans")
public class LearningPlan {
    
    @Id
    private String id;
    
    private String userId;
    private String userFirstName;
    private String userLastName;
    
    private String title;
    private LocalDate deadline;
    private List<Topic> topics = new ArrayList<>();
    
    // Progress tracking fields
    private double progressPercentage = 0.0;
    private int completedTopics = 0;
    
    // Sharing capability
    private boolean isShared = false;
    private String description;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Transient field (not stored in the database)
    private transient boolean userOwnsIt;
    
    public static class Topic {
        private String id;
        private String name;
        private List<String> resourceUrls = new ArrayList<>();
        private boolean completed = false;
        private LocalDateTime completedAt;
        private int estimatedMinutes = 60; // Default 1 hour per topic
        
        public Topic() {
            this.id = java.util.UUID.randomUUID().toString();
        }
        
        public Topic(String name) {
            this();
            this.name = name;
        }
        
        // Getters and setters
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public List<String> getResourceUrls() {
            return resourceUrls;
        }
        
        public void setResourceUrls(List<String> resourceUrls) {
            this.resourceUrls = resourceUrls;
        }
        
        public void addResourceUrl(String url) {
            this.resourceUrls.add(url);
        }
        
        public boolean removeResourceUrl(String url) {
            return this.resourceUrls.remove(url);
        }
        
        public boolean isCompleted() {
            return completed;
        }
        
        public void setCompleted(boolean completed) {
            this.completed = completed;
            if (completed && completedAt == null) {
                this.completedAt = LocalDateTime.now();
            } else if (!completed) {
                this.completedAt = null;
            }
        }
        
        public LocalDateTime getCompletedAt() {
            return completedAt;
        }
        
        public void setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
        }
        
        public int getEstimatedMinutes() {
            return estimatedMinutes;
        }
        
        public void setEstimatedMinutes(int estimatedMinutes) {
            this.estimatedMinutes = estimatedMinutes;
        }
    }
    
    // Constructors
    public LearningPlan() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public LearningPlan(String userId, String title, LocalDate deadline) {
        this();
        this.userId = userId;
        this.title = title;
        this.deadline = deadline;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserFirstName() {
        return userFirstName;
    }
    
    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }
    
    public String getUserLastName() {
        return userLastName;
    }
    
    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public LocalDate getDeadline() {
        return deadline;
    }
    
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
    
    public List<Topic> getTopics() {
        return topics;
    }
    
    public void setTopics(List<Topic> topics) {
        this.topics = topics;
        updateProgress();
    }
    
    public void addTopic(Topic topic) {
        this.topics.add(topic);
        updateProgress();
    }
    
    public boolean removeTopic(String topicId) {
        boolean removed = this.topics.removeIf(topic -> topic.getId().equals(topicId));
        if (removed) {
            updateProgress();
        }
        return removed;
    }
    
    public double getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    
    public int getCompletedTopics() {
        return completedTopics;
    }
    
    public void setCompletedTopics(int completedTopics) {
        this.completedTopics = completedTopics;
    }
    
    /**
     * Updates the progress percentage based on completed topics
     */
    public void updateProgress() {
        if (topics.isEmpty()) {
            this.progressPercentage = 0.0;
            this.completedTopics = 0;
            return;
        }
        
        this.completedTopics = (int) topics.stream()
                .filter(Topic::isCompleted)
                .count();
        
        this.progressPercentage = (double) completedTopics / topics.size() * 100;
    }
    
    /**
     * Marks a topic as complete or incomplete and updates progress
     * 
     * @param topicId the ID of the topic to update
     * @param completed the new completion status
     * @return true if the topic was found and updated, false otherwise
     */
    public boolean updateTopicCompletionStatus(String topicId, boolean completed) {
        for (Topic topic : topics) {
            if (topic.getId().equals(topicId)) {
                topic.setCompleted(completed);
                updateProgress();
                return true;
            }
        }
        return false;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean isShared() {
        return isShared;
    }
    
    public void setShared(boolean shared) {
        isShared = shared;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isCompleted() {
        return topics.size() > 0 && completedTopics == topics.size();
    }
    
    public boolean isUserOwnsIt() {
        return userOwnsIt;
    }
    
    public void setUserOwnsIt(boolean userOwnsIt) {
        this.userOwnsIt = userOwnsIt;
    }
} 