package com.example.LearningMedia.service.impl;

import com.example.LearningMedia.dto.CommentResponse;
import com.example.LearningMedia.dto.PostRequest;
import com.example.LearningMedia.dto.PostResponse;
import com.example.LearningMedia.model.Comment;
import com.example.LearningMedia.model.Post;
import com.example.LearningMedia.model.User;
import com.example.LearningMedia.model.UserFollow;
import com.example.LearningMedia.repository.PostRepository;
import com.example.LearningMedia.repository.UserFollowRepository;
import com.example.LearningMedia.repository.UserRepository;
import com.example.LearningMedia.service.CommentService;
import com.example.LearningMedia.service.NotificationService;
import com.example.LearningMedia.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
    
    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);
    
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final CommentService commentService;
    private final NotificationService notificationService;
    
    @Autowired
    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository, 
                         UserFollowRepository userFollowRepository, CommentService commentService,
                         NotificationService notificationService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.userFollowRepository = userFollowRepository;
        this.commentService = commentService;
        this.notificationService = notificationService;
    }
    
    @Override
    public Post createPost(String userId, PostRequest postRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        Post post = new Post();
        post.setUserId(userId);
        post.setUserFirstName(user.getFirstName());
        post.setUserLastName(user.getLastName());
        post.setUserProfilePicture(user.getProfilePicture());
        post.setDescription(postRequest.getDescription());
        
        return postRepository.save(post);
    }
    
    @Override
    public Optional<Post> getPostById(String postId) {
        return postRepository.findById(postId);
    }
    
    @Override
    public Page<PostResponse> getAllPosts(String currentUserId, Pageable pageable) {
        Page<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        return mapPostsToPostResponses(posts, currentUserId);
    }
    
    @Override
    public Page<PostResponse> getPostsByUserId(String userId, String currentUserId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return mapPostsToPostResponses(posts, currentUserId);
    }
    
    @Override
    public List<PostResponse> getFeedForUser(String userId, Pageable pageable) {
        // Get list of followed users
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return new ArrayList<>();
        }
        
        User user = optionalUser.get();
        
        // Get followed users from the UserFollow repository
        List<UserFollow> follows = userFollowRepository.findByFollowerId(userId);
        List<String> followedUserIds = follows.stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toList());
        
        // Add the user's own ID to show their posts in their feed too
        followedUserIds.add(userId);
        
        // Find posts from followed users
        List<Post> posts = postRepository.findByUserIdInOrderByCreatedAtDesc(followedUserIds, pageable);
        
        List<PostResponse> postResponses = posts.stream()
                .map(post -> convertToPostResponse(post, userId))
                .collect(Collectors.toList());
        
        return postResponses;
    }
    
    @Override
    @Transactional
    public Post addMediaToPost(String postId, String userId, MultipartFile file) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));
        
        // Check if user is the owner of the post
        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User does not have permission to add media to this post");
        }
        
        // Check if post already has 3 media items
        if (post.getMediaItems().size() >= 3) {
            throw new IllegalArgumentException("Post already has the maximum number of media items (3)");
        }
        
        try {
            String contentType = file.getContentType();
            if (contentType == null) {
                throw new IllegalArgumentException("Content type cannot be determined");
            }
            
            // Validate content type
            if (contentType.startsWith("image/")) {
                // Allowed image types
                if (!isAllowedImageType(contentType)) {
                    throw new IllegalArgumentException("Image type not supported. Allowed formats: JPEG, PNG, GIF");
                }
            } else if (contentType.startsWith("video/")) {
                // Allowed video types
                if (!isAllowedVideoType(contentType)) {
                    throw new IllegalArgumentException("Video type not supported. Allowed formats: MP4, WebM");
                }
                
                // For videos, we should check duration, but since we can't do that server-side easily,
                // we'll add a note in the contentType that client should validate 30 sec max
                // In a real implementation, we would use a media processing library to check duration
                
                // Check file size as a rough proxy for duration (10MB limit for videos)
                if (file.getSize() > 10 * 1024 * 1024) {
                    throw new IllegalArgumentException("Video file too large. Maximum size is 10MB (~30 seconds)");
                }
            } else {
                throw new IllegalArgumentException("Only image and video files are allowed");
            }
            
            // Convert to Base64 and store
            byte[] bytes = file.getBytes();
            String base64Data = Base64.getEncoder().encodeToString(bytes);
            String mediaUrl = "data:" + contentType + ";base64," + base64Data;
            
            // Determine media type
            String mediaType = contentType.startsWith("image/") ? "image" : "video";
            
            // Create and add media item
            Post.Media media = new Post.Media(mediaUrl, mediaType, contentType);
            post.addMediaItem(media);
            post.setUpdatedAt(LocalDateTime.now());
            
            return postRepository.save(post);
        } catch (IOException e) {
            logger.error("Error processing media file", e);
            throw new RuntimeException("Error processing media file: " + e.getMessage(), e);
        }
    }
    
    // Helper method to check if image type is allowed
    private boolean isAllowedImageType(String contentType) {
        return contentType.equals("image/jpeg") || 
               contentType.equals("image/jpg") || 
               contentType.equals("image/png") || 
               contentType.equals("image/gif");
    }
    
    // Helper method to check if video type is allowed
    private boolean isAllowedVideoType(String contentType) {
        return contentType.equals("video/mp4") || 
               contentType.equals("video/webm") || 
               contentType.equals("video/quicktime");
    }
    
    @Override
    @Transactional
    public boolean deletePost(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));
        
        // Check if user is the owner of the post
        if (!post.getUserId().equals(userId)) {
            return false;
        }
        
        // Delete all comments for this post
        commentService.deleteCommentsByPostId(postId);
        
        // Then delete the post
        postRepository.delete(post);
        return true;
    }
    
    @Override
    @Transactional
    public void deleteMediaFromPost(String postId, String mediaId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));
        
        // Check if user is the owner of the post
        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User does not have permission to delete media from this post");
        }
        
        // Remove media item
        post.getMediaItems().removeIf(media -> media.getId().equals(mediaId));
        post.setUpdatedAt(LocalDateTime.now());
        
        postRepository.save(post);
    }
    
    @Override
    public List<Post> findByUserId(String userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged()).getContent();
    }
    
    @Override
    @Transactional
    public Post updatePost(String postId, String userId, PostRequest postRequest) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));
        
        // Check if user is the owner of the post
        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User does not have permission to update this post");
        }
        
        // Update the description field
        post.setDescription(postRequest.getDescription());
        post.setUpdatedAt(LocalDateTime.now());
        
        // Handle mediaIds if provided in the request
        // This is for the case when we want to reorder media or only keep certain ones
        if (postRequest.getMediaIds() != null && !postRequest.getMediaIds().isEmpty()) {
            // Filter existing media items to only include those in the request
            List<Post.Media> updatedMediaItems = new ArrayList<>();
            
            // Create a map of existing media items by ID for quick lookup
            Map<String, Post.Media> existingMediaMap = post.getMediaItems().stream()
                    .collect(Collectors.toMap(Post.Media::getId, media -> media));
            
            // Add media items in the order specified in the request
            for (String mediaId : postRequest.getMediaIds()) {
                Post.Media media = existingMediaMap.get(mediaId);
                if (media != null) {
                    updatedMediaItems.add(media);
                }
            }
            
            // Update media items with the ordered list
            post.setMediaItems(updatedMediaItems);
        }
        
        return postRepository.save(post);
    }
    
    @Override
    @Transactional
    public boolean likePost(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));
        
        boolean added = post.addLike(userId);
        
        if (added) {
            // Create notification for post owner
            notificationService.createPostLikeNotification(postId, userId);
            
            post.setUpdatedAt(LocalDateTime.now());
            postRepository.save(post);
        }
        
        return added;
    }
    
    @Override
    @Transactional
    public boolean unlikePost(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));
        
        if (post.removeLike(userId)) {
            post.setUpdatedAt(LocalDateTime.now());
            postRepository.save(post);
            return true;
        }
        
        return false; // If the user hadn't liked the post
    }
    
    @Override
    public int getLikeCount(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));
        
        return post.getLikeCount();
    }
    
    private Page<PostResponse> mapPostsToPostResponses(Page<Post> posts, String currentUserId) {
        List<PostResponse> postResponses = posts.getContent().stream()
                .map(post -> convertToPostResponse(post, currentUserId))
                .collect(Collectors.toList());
        
        return new PageImpl<>(postResponses, posts.getPageable(), posts.getTotalElements());
    }
    
    private PostResponse convertToPostResponse(Post post, String currentUserId) {
        // Get comments for the post
        List<Comment> comments = commentService.getCommentsByPostId(post.getId());
        
        // Convert to comment responses
        List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> CommentResponse.fromComment(comment, currentUserId))
                .collect(Collectors.toList());
        
        // Create post response with comments
        return PostResponse.fromPost(post, currentUserId, commentResponses);
    }
} 