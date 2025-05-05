package com.example.LearningMedia.controller;

import com.example.LearningMedia.dto.PostRequest;
import com.example.LearningMedia.dto.PostResponse;
import com.example.LearningMedia.model.Post;
import com.example.LearningMedia.model.User;
import com.example.LearningMedia.service.PostService;
import com.example.LearningMedia.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);
    
    private final PostService postService;
    private final UserService userService;
    
    @Autowired
    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }
    
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostRequest postRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Find the user by email first to get the actual user ID (OAuth2)
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            // Get the actual user ID (OAuth2)
            String userId = userOptional.get().getId();
            
            Post post = postService.createPost(userId, postRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Post created successfully");
            response.put("postId", post.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating post", e);
            return ResponseEntity.badRequest().body("Failed to create post: " + e.getMessage());
        }
    }
    
    @PostMapping("/with-media")
    public ResponseEntity<?> createPostWithMedia(
            @RequestParam("description") String description,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            // Get the user ID (OAuth2)
            String userId = userOptional.get().getId();
            
            // Validate number of files uploaded
            if (files != null && files.size() > 3) {
                return ResponseEntity.badRequest().body("Maximum 3 media files allowed per post");
            }
            
            // Create the post with new description
            PostRequest postRequest = new PostRequest();
            postRequest.setDescription(description);
            Post post = postService.createPost(userId, postRequest);
            
            // Add media files if provided (max 3 files)
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    postService.addMediaToPost(post.getId(), userId, file);
                }
                // Refresh post after adding media
                post = postService.getPostById(post.getId()).orElse(post);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Post created successfully with media");
            response.put("postId", post.getId());
            response.put("mediaCount", post.getMediaItems().size());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating post with media", e);
            return ResponseEntity.badRequest().body("Failed to create post: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Get the actual user ID from system
            String userId = userOptional.get().getId();
            
            Pageable pageable = PageRequest.of(page, size);
            Page<PostResponse> posts = postService.getAllPosts(userId, pageable);
            
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            logger.error("Error fetching posts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/feed")
    public ResponseEntity<List<PostResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Find the user by email first to get the actual user ID
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Get the actual user ID
            String userId = userOptional.get().getId();
            
            Pageable pageable = PageRequest.of(page, size);
            List<PostResponse> feed = postService.getFeedForUser(userId, pageable);
            
            return ResponseEntity.ok(feed);
        } catch (Exception e) {
            logger.error("Error fetching feed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponse>> getPostsByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
        
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Get the actual user ID from the system
            String currentUserId = userOptional.get().getId();
            
            Pageable pageable = PageRequest.of(page, size);
            Page<PostResponse> posts = postService.getPostsByUserId(userId, currentUserId, pageable);
            
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            logger.error("Error fetching posts for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable String postId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Find the user by email first to get the actual user ID
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            // Get the actual user ID
            String userId = userOptional.get().getId();
            
            return postService.getPostById(postId)
                    .map(post -> {
                        PostResponse postResponse = PostResponse.fromPost(post, userId);
                        return ResponseEntity.ok(postResponse);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching post {}", postId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching post: " + e.getMessage());
        }
    }
    
    @PostMapping("/{postId}/media")
    public ResponseEntity<?> addMediaToPost(
            @PathVariable String postId,
            @RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Find the user by email first to get the actual user ID
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            // Get the actual user ID
            String userId = userOptional.get().getId();
            
            Post post = postService.addMediaToPost(postId, userId, file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Media added successfully");
            response.put("mediaItems", post.getMediaItems());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error adding media to post {}", postId, e);
            return ResponseEntity.badRequest().body("Failed to add media: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{postId}/media/{mediaId}")
    public ResponseEntity<?> deleteMediaFromPost(
            @PathVariable String postId,
            @PathVariable String mediaId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Find the user by email first to get the actual user ID
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            // Get the actual user ID
            String userId = userOptional.get().getId();
            
            postService.deleteMediaFromPost(postId, mediaId, userId);
            
            return ResponseEntity.ok().body("Media deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting media {} from post {}", mediaId, postId, e);
            return ResponseEntity.badRequest().body("Failed to delete media: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable String postId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Find the user by email first to get the actual user ID
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            // Get the actual user ID
            String userId = userOptional.get().getId();
            
            boolean deleted = postService.deletePost(postId, userId);
            
            if (deleted) {
                return ResponseEntity.ok().body("Post deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to delete this post");
            }
        } catch (Exception e) {
            logger.error("Error deleting post {}", postId, e);
            return ResponseEntity.badRequest().body("Failed to delete post: " + e.getMessage());
        }
    }
    
    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable String postId,
            @RequestBody PostRequest postRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Find the user by email first to get the actual user ID
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            // Get the actual user ID
            String userId = userOptional.get().getId();
            
            Post updatedPost = postService.updatePost(postId, userId, postRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Post updated successfully");
            response.put("postId", updatedPost.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating post {}", postId, e);
            return ResponseEntity.badRequest().body("Failed to update post: " + e.getMessage());
        }
    }
    
    @PostMapping("/{postId}/update-with-media")
    public ResponseEntity<?> updatePostWithMedia(
            @PathVariable String postId,
            @RequestParam("description") String description,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Find the user by email first to get the actual user ID
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            // Get the actual user ID
            String userId = userOptional.get().getId();
            
            // Get the post
            Optional<Post> postOptional = postService.getPostById(postId);
            
            if (postOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Post post = postOptional.get();
            
            // Check if user is authorized to update this post
            if (!post.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to update this post");
            }
            
            // Update the description
            PostRequest postRequest = new PostRequest();
            postRequest.setDescription(description);
            post = postService.updatePost(postId, userId, postRequest);
            
            // Add new media files if provided
            if (files != null && !files.isEmpty()) {
                int totalMediaCount = post.getMediaItems().size() + files.size();
                
                // Check if total media would exceed limit
                if (totalMediaCount > 3) {
                    return ResponseEntity.badRequest().body("Maximum 3 media files allowed per post. Current total would be " + totalMediaCount);
                }
                
                for (MultipartFile file : files) {
                    postService.addMediaToPost(post.getId(), userId, file);
                }
                
                // Refresh post after adding media
                post = postService.getPostById(post.getId()).orElse(post);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Post updated successfully with media");
            response.put("postId", post.getId());
            response.put("mediaCount", post.getMediaItems().size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating post {} with media", postId, e);
            return ResponseEntity.badRequest().body("Failed to update post: " + e.getMessage());
        }
    }
    
    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable String postId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Find the user by email to get the user ID
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            String userId = userOpt.get().getId();
            
            boolean liked = postService.likePost(postId, userId);
            int likeCount = postService.getLikeCount(postId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("liked", liked);
            response.put("likeCount", likeCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error liking post {}", postId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to like post: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<?> unlikePost(@PathVariable String postId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Find the user by email to get the user ID
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            String userId = userOpt.get().getId();
            
            boolean unliked = postService.unlikePost(postId, userId);
            int likeCount = postService.getLikeCount(postId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("unliked", unliked);
            response.put("likeCount", likeCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error unliking post {}", postId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to unlike post: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{postId}/likes")
    public ResponseEntity<?> getPostLikes(@PathVariable String postId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Find the user by email to get the user ID
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            String userId = userOpt.get().getId();
            
            int likeCount = postService.getLikeCount(postId);
            Optional<Post> postOpt = postService.getPostById(postId);
            
            if (postOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
            }
            
            boolean isLikedByCurrentUser = postOpt.get().isLikedByUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("likeCount", likeCount);
            response.put("likedByCurrentUser", isLikedByCurrentUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting post likes {}", postId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get post likes: " + e.getMessage()));
        }
    }
} 