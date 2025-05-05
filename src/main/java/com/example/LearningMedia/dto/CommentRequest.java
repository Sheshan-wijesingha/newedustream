package com.example.LearningMedia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {
    
    @NotBlank(message = "Comment content cannot be empty")
    @Size(min = 1, max = 500, message = "Comment must be between 1 and 500 characters")
    private String content;
    
    public CommentRequest() {
    }
    
    public CommentRequest(String content) {
        this.content = content;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
} 