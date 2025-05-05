package com.example.LearningMedia.dto;

import java.util.ArrayList;
import java.util.List;

public class PostRequest {
    
    private String description;
    private List<String> mediaIds = new ArrayList<>();
    
    public PostRequest() {
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getMediaIds() {
        return mediaIds;
    }
    
    public void setMediaIds(List<String> mediaIds) {
        this.mediaIds = mediaIds;
    }
} 