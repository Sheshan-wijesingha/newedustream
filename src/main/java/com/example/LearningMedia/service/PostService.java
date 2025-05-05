package com.example.LearningMedia.service;

import com.example.LearningMedia.dto.PostRequest;
import com.example.LearningMedia.dto.PostResponse;
import com.example.LearningMedia.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface PostService {
    
    Post createPost(String userId, PostRequest postRequest);
    
    Optional<Post> getPostById(String postId);
    
    Page<PostResponse> getAllPosts(String currentUserId, Pageable pageable);
    
    Page<PostResponse> getPostsByUserId(String userId, String currentUserId, Pageable pageable);
    
    List<PostResponse> getFeedForUser(String userId, Pageable pageable);
    
    Post addMediaToPost(String postId, String userId, MultipartFile file);
    
    boolean deletePost(String postId, String userId);
    
    void deleteMediaFromPost(String postId, String mediaId, String userId);
    
    List<Post> findByUserId(String userId);
    
    Post updatePost(String postId, String userId, PostRequest postRequest);
    
    boolean likePost(String postId, String userId);
    
    boolean unlikePost(String postId, String userId);
    
    int getLikeCount(String postId);
} 