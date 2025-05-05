// Main JavaScript file for Learning Media

document.addEventListener('DOMContentLoaded', () => {
    // Initialize components based on page
    const currentPage = getCurrentPage();
    
    if (currentPage === 'dashboard' || currentPage === 'feed') {
        initializeFeed();
    }
    
    // Initialize tooltips and popovers
    initializeBootstrapComponents();
});

// Get current page from URL
function getCurrentPage() {
    const path = window.location.pathname;
    if (path === '/' || path === '/dashboard') {
        return 'dashboard';
    } else if (path.includes('/feed')) {
        return 'feed';
    } else if (path.includes('/profile')) {
        return 'profile';
    }
    return '';
}

// Initialize Bootstrap components
function initializeBootstrapComponents() {
    // Initialize tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Initialize popovers
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
}

// Initialize feed
function initializeFeed() {
    const feedContainer = document.getElementById('feed-container');
    if (!feedContainer) return;
    
    // Get current user ID
    const currentUserIdElement = document.getElementById('current-user-id');
    window.currentUserId = currentUserIdElement ? currentUserIdElement.value : '';
    
    let page = 0;
    const size = 5;
    let loading = false;
    let hasMorePosts = true;
    
    // Function to load feed
    async function loadFeedPosts() {
        if (loading || !hasMorePosts) return;
        
        loading = true;
        const loadingIndicator = document.getElementById('feed-loading');
        if (loadingIndicator) loadingIndicator.style.display = 'block';
        
        try {
            const posts = await feedModule.loadFeed(page, size);
            
            if (posts.length === 0) {
                hasMorePosts = false;
                if (page === 0) {
                    feedContainer.innerHTML = '<div class="text-center p-4"><p class="text-muted">No posts yet. Create your first post!</p></div>';
                }
            } else {
                posts.forEach(post => {
                    const postElement = feedModule.createPostElement(post);
                    feedContainer.appendChild(postElement);
                });
                
                page++;
            }
        } catch (error) {
            console.error('Error loading feed:', error);
        } finally {
            loading = false;
            if (loadingIndicator) loadingIndicator.style.display = 'none';
        }
    }
    
    // Initialize create post form
    initializeCreatePostForm();
    
    // Load initial posts
    loadFeedPosts();
    
    // Infinite scroll
    window.addEventListener('scroll', () => {
        const { scrollTop, scrollHeight, clientHeight } = document.documentElement;
        
        if (scrollTop + clientHeight >= scrollHeight - 200 && !loading && hasMorePosts) {
            loadFeedPosts();
        }
    });
}

// Initialize create post form
function initializeCreatePostForm() {
    const createPostForm = document.getElementById('create-post-form');
    if (!createPostForm) return;
    
    const postTextarea = document.getElementById('post-textarea');
    const mediaInput = document.getElementById('media-input');
    const mediaPreview = document.getElementById('media-preview');
    const submitButton = document.getElementById('submit-post');
    
    let selectedFiles = [];
    
    // Handle media selection
    mediaInput.addEventListener('change', () => {
        const files = Array.from(mediaInput.files);
        
        // Check if total number of files exceeds 3
        if (selectedFiles.length + files.length > 3) {
            showToast('You can only upload a maximum of 3 files per post.');
            return;
        }
        
        // Process each file
        files.forEach(file => {
            if (!file.type.startsWith('image/') && !file.type.startsWith('video/')) {
                showToast('Only images and videos are allowed.');
                return;
            }
            
            // Check file size (5MB max)
            if (file.size > 5 * 1024 * 1024) {
                showToast('Files must be less than 5MB.');
                return;
            }
            
            // Add file to selected files
            selectedFiles.push(file);
            
            // Create preview
            const reader = new FileReader();
            reader.onload = (e) => {
                const previewItem = document.createElement('div');
                previewItem.className = 'media-preview-item';
                
                if (file.type.startsWith('image/')) {
                    previewItem.innerHTML = `
                        <img src="${e.target.result}" alt="Media preview">
                        <button type="button" class="remove-media" data-index="${selectedFiles.length - 1}">
                            <i class="bi bi-x"></i>
                        </button>
                    `;
                } else {
                    previewItem.innerHTML = `
                        <video controls>
                            <source src="${e.target.result}" type="${file.type}">
                        </video>
                        <button type="button" class="remove-media" data-index="${selectedFiles.length - 1}">
                            <i class="bi bi-x"></i>
                        </button>
                    `;
                }
                
                mediaPreview.appendChild(previewItem);
                
                // Add remove event listener
                const removeButton = previewItem.querySelector('.remove-media');
                removeButton.addEventListener('click', () => {
                    const index = parseInt(removeButton.dataset.index);
                    selectedFiles.splice(index, 1);
                    previewItem.remove();
                    
                    // Update indexes for remaining remove buttons
                    const removeButtons = mediaPreview.querySelectorAll('.remove-media');
                    removeButtons.forEach((button, i) => {
                        button.dataset.index = i;
                    });
                });
            };
            
            reader.readAsDataURL(file);
        });
    });
    
    // Handle form submission
    createPostForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const description = postTextarea.value.trim();
        if (!description && selectedFiles.length === 0) {
            showToast('Please enter a description or add media.');
            return;
        }
        
        submitButton.disabled = true;
        submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Posting...';
        
        try {
            const postId = await feedModule.createPost(description, selectedFiles);
            
            if (postId) {
                // Clear form
                postTextarea.value = '';
                selectedFiles = [];
                mediaPreview.innerHTML = '';
                
                // Reload feed
                const feedContainer = document.getElementById('feed-container');
                if (feedContainer) {
                    feedContainer.innerHTML = '';
                    window.page = 0;
                    window.hasMorePosts = true;
                    loadFeedPosts();
                }
                
                showToast('Post created successfully!', 'success');
            }
        } catch (error) {
            console.error('Error creating post:', error);
            showToast('Error creating post. Please try again.');
        } finally {
            submitButton.disabled = false;
            submitButton.innerHTML = 'Post';
        }
    });
}

// Show toast notification
function showToast(message, type = 'danger') {
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
} 