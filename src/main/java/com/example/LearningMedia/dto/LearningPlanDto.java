package com.example.LearningMedia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LearningPlanDto {
    
    private String id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotNull(message = "Deadline is required")
    @FutureOrPresent(message = "Deadline must be today or in the future")
    private LocalDate deadline;
    
    @NotEmpty(message = "At least one topic is required")
    private List<TopicDto> topics = new ArrayList<>();
    
    // Progress tracking
    private double progressPercentage;
    private int completedTopics;
    
    // Sharing capability
    private boolean isShared = false;
    private String description;
    
    public static class TopicDto {
        private String id;
        
        @NotBlank(message = "Topic name is required")
        private String name;
        
        @NotEmpty(message = "At least one resource URL is required")
        private List<String> resourceUrls = new ArrayList<>();
        
        private boolean completed;
        private LocalDateTime completedAt;
        private int estimatedMinutes = 60; // Default 1 hour
        
        // Constructors
        public TopicDto() {
        }
        
        public TopicDto(String name) {
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
        
        public boolean isCompleted() {
            return completed;
        }
        
        public void setCompleted(boolean completed) {
            this.completed = completed;
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
    public LearningPlanDto() {
    }
    
    public LearningPlanDto(String title, LocalDate deadline) {
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
    
    public List<TopicDto> getTopics() {
        return topics;
    }
    
    public void setTopics(List<TopicDto> topics) {
        this.topics = topics;
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
} 