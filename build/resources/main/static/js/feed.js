// Feed functionality for Learning Media
const feedModule = (() => {
    // API endpoints
    const API = {
        POSTS: '/api/posts',
        FEED: '/api/posts/feed'
    };

    // Create post
    const createPost = async (description, files = []) => {
        try {
            // First create the post
            const postResponse = await fetch(API.POSTS, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ description })
            });

            if (!postResponse.ok) throw new Error('Failed to create post');
            
            const postData = await postResponse.json();
            const postId = postData.postId;
            
            // If there are files, upload them
            if (files.length > 0) {
                for (const file of files) {
                    if (!file) continue;
                    
                    const formData = new FormData();
                    formData.append('file', file);
                    
                    await fetch(`${API.POSTS}/${postId}/media`, {
                        method: 'POST',
                        body: formData
                    });
                }
            }
            
            return postId;
        } catch (error) {
            console.error('Error creating post:', error);
            showToast('Error creating post. Please try again.');
            return null;
        }
    };

    // Load feed posts
    const loadFeed = async (page = 0, size = 5) => {
        try {
            const response = await fetch(`${API.FEED}?page=${page}&size=${size}`);
            if (!response.ok) throw new Error('Failed to load feed');
            return await response.json();
        } catch (error) {
            console.error('Error loading feed:', error);
            showToast('Error loading feed. Please try again.');
            return [];
        }
    };

    // Delete a post
    const deletePost = async (postId) => {
        try {
            const response = await fetch(`${API.POSTS}/${postId}`, {
                method: 'DELETE'
            });
            
            if (!response.ok) throw new Error('Failed to delete post');
            return true;
        } catch (error) {
            console.error('Error deleting post:', error);
            showToast('Error deleting post. Please try again.');
            return false;
        }
    };

    // Helper function to show toast notifications
    const showToast = (message, type = 'danger') => {
        const toastContainer = document.getElementById('toast-container');
        if (!toastContainer) return;
        
        const toast = document.createElement('div');
        toast.className = `toast align-items-center text-white bg-${type} border-0`;
        toast.setAttribute('role', 'alert');
        toast.setAttribute('aria-live', 'assertive');
        toast.setAttribute('aria-atomic', 'true');
        
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        `;
        
        toastContainer.appendChild(toast);
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();
        
        toast.addEventListener('hidden.bs.toast', () => {
            toast.remove();
        });
    };

    // Create post element
    const createPostElement = (post) => {
        const postElement = document.createElement('div');
        postElement.className = 'card post-card mb-4 animate__animated animate__fadeIn';
        postElement.dataset.postId = post.id;
        
        let mediaHtml = '';
        if (post.mediaItems && post.mediaItems.length > 0) {
            // If there's just one image
            if (post.mediaItems.length === 1) {
                const media = post.mediaItems[0];
                if (media.type === 'image') {
                    mediaHtml = `<img src="${media.url}" alt="Post image" class="post-media img-fluid mb-3">`;
                } else if (media.type === 'video') {
                    mediaHtml = `
                        <video class="post-media img-fluid mb-3" controls>
                            <source src="${media.url}" type="${media.contentType}">
                            Your browser does not support the video tag.
                        </video>
                    `;
                }
            } else {
                // Multiple media items - create a carousel
                mediaHtml = `
                <div id="carousel-${post.id}" class="carousel slide mb-3" data-bs-ride="carousel">
                    <div class="carousel-indicators">
                        ${post.mediaItems.map((_, index) => 
                            `<button type="button" data-bs-target="#carousel-${post.id}" data-bs-slide-to="${index}" 
                            ${index === 0 ? 'class="active" aria-current="true"' : ''} aria-label="Slide ${index+1}"></button>`
                        ).join('')}
                    </div>
                    <div class="carousel-inner">
                        ${post.mediaItems.map((media, index) => `
                            <div class="carousel-item ${index === 0 ? 'active' : ''}">
                                ${media.type === 'image' 
                                    ? `<img src="${media.url}" class="d-block w-100" alt="Post image">`
                                    : `<video class="d-block w-100" controls>
                                        <source src="${media.url}" type="${media.contentType}">
                                        Your browser does not support the video tag.
                                    </video>`
                                }
                            </div>
                        `).join('')}
                    </div>
                    <button class="carousel-control-prev" type="button" data-bs-target="#carousel-${post.id}" data-bs-slide="prev">
                        <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                        <span class="visually-hidden">Previous</span>
                    </button>
                    <button class="carousel-control-next" type="button" data-bs-target="#carousel-${post.id}" data-bs-slide="next">
                        <span class="carousel-control-next-icon" aria-hidden="true"></span>
                        <span class="visually-hidden">Next</span>
                    </button>
                </div>`;
            }
        }
        
        // Create delete button if current user is the post author
        const deleteButton = post.userId === currentUserId ? 
            `<button class="btn btn-sm text-danger delete-post" title="Delete post">
                <i class="bi bi-trash"></i>
            </button>` : '';
        
        postElement.innerHTML = `
            <div class="card-header">
                <div class="d-flex align-items-center">
                    <div class="avatar me-2">
                        ${post.userProfilePicture 
                            ? `<img src="${post.userProfilePicture}" alt="${post.userFullName}" class="rounded-circle" width="40" height="40">` 
                            : `<div class="no-profile rounded-circle d-flex align-items-center justify-content-center" style="width:40px;height:40px;background:#4481eb;color:white">
                                <i class="bi bi-person"></i>
                               </div>`
                        }
                    </div>
                    <div>
                        <h6 class="mb-0">${post.userFullName}</h6>
                        <small class="text-muted">${post.timeAgo}</small>
                    </div>
                    <div class="ms-auto">
                        ${deleteButton}
                    </div>
                </div>
            </div>
            <div class="card-body">
                <p class="card-text">${post.description}</p>
                ${mediaHtml}
            </div>
        `;
        
        // Attach event listeners
        const deleteBtn = postElement.querySelector('.delete-post');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', async function() {
                if (confirm('Are you sure you want to delete this post?')) {
                    const postId = postElement.dataset.postId;
                    const deleted = await deletePost(postId);
                    
                    if (deleted) {
                        postElement.classList.add('animate__fadeOut');
                        setTimeout(() => {
                            postElement.remove();
                        }, 500);
                    }
                }
            });
        }
        
        return postElement;
    };

    // Initialize feed
    const initFeed = async () => {
        const feedContainer = document.getElementById('feed-container');
        const loadingIndicator = document.getElementById('loading-indicator');
        
        if (!feedContainer) return;
        
        try {
            // Show loading indicator
            if (loadingIndicator) loadingIndicator.style.display = 'block';
            
            const posts = await loadFeed();
            
            // Hide loading indicator
            if (loadingIndicator) loadingIndicator.style.display = 'none';
            
            if (!posts || posts.length === 0) {
                feedContainer.innerHTML = `
                    <div class="no-posts">
                        <i class="bi bi-collection"></i>
                        <h4>No posts yet</h4>
                        <p>Follow other users or create your first post to see content here</p>
                    </div>
                `;
                return;
            }
            
            // Clear container
            feedContainer.innerHTML = '';
            
            // Add posts to feed
            posts.forEach(post => {
                const postElement = createPostElement(post);
                feedContainer.appendChild(postElement);
            });
        } catch (error) {
            console.error('Error initializing feed:', error);
            if (loadingIndicator) loadingIndicator.style.display = 'none';
            
            feedContainer.innerHTML = `
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle-fill"></i>
                    Error loading feed. Please try refreshing the page.
                </div>
            `;
        }
    };

    // Public methods
    return {
        init: initFeed,
        createPost,
        loadFeed,
        deletePost
    };
})();

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    feedModule.init();
    
    // Setup post creation functionality
    window.createPost = async function() {
        const descriptionInput = document.getElementById('postDescription');
        const description = descriptionInput.value.trim();
        
        if (!description) {
            feedModule.showToast('Please write something for your post.', 'warning');
            return;
        }
        
        // Get selected files
        const fileInput = document.getElementById('mediaUpload');
        const files = fileInput.files;
        
        // Show loading state
        const shareButton = document.querySelector('#createPostForm button[type="button"]');
        const originalText = shareButton.innerHTML;
        shareButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Sharing...';
        shareButton.disabled = true;
        
        try {
            // Create post
            const postId = await feedModule.createPost(description, files);
            
            if (postId) {
                // Reset form
                descriptionInput.value = '';
                fileInput.value = '';
                document.getElementById('mediaPreviewContainer').innerHTML = '';
                document.getElementById('mediaCounter').textContent = '0/3 media items';
                
                // Show success
                feedModule.showToast('Post shared successfully!', 'success');
                
                // Reload feed to show new post
                await feedModule.init();
            }
        } catch (error) {
            console.error('Error in post creation:', error);
            feedModule.showToast('An error occurred while sharing your post.', 'danger');
        } finally {
            // Reset button
            shareButton.innerHTML = originalText;
            shareButton.disabled = false;
        }
    };
    
    // Media preview functionality
    window.previewMedia = function(input) {
        const previewContainer = document.getElementById('mediaPreviewContainer');
        const mediaCounter = document.getElementById('mediaCounter');
        
        if (input.files.length > 3) {
            feedModule.showToast('You can only upload up to 3 media items per post.', 'warning');
            input.value = '';
            return;
        }
        
        previewContainer.innerHTML = '';
        mediaCounter.textContent = `${input.files.length}/3 media items`;
        
        for (let i = 0; i < input.files.length; i++) {
            const file = input.files[i];
            const reader = new FileReader();
            
            reader.onload = function(e) {
                const previewItem = document.createElement('div');
                previewItem.className = 'preview-item';
                
                if (file.type.startsWith('image/')) {
                    previewItem.innerHTML = `
                        <img src="${e.target.result}" alt="Preview">
                        <span class="remove-media" data-index="${i}">×</span>
                    `;
                } else if (file.type.startsWith('video/')) {
                    previewItem.innerHTML = `
                        <video src="${e.target.result}" muted></video>
                        <span class="remove-media" data-index="${i}">×</span>
                    `;
                }
                
                previewContainer.appendChild(previewItem);
                
                // Add remove functionality
                const removeBtn = previewItem.querySelector('.remove-media');
                removeBtn.addEventListener('click', function() {
                    // Since we can't directly modify a FileList, we need to reset the input
                    // and manually add back all files except the one to remove
                    input.value = '';
                    mediaCounter.textContent = `0/3 media items`;
                    previewContainer.innerHTML = '';
                });
            };
            
            reader.readAsDataURL(file);
        }
    };
}); 