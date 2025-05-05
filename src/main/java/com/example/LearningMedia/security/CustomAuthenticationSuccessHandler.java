package com.example.LearningMedia.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom authentication success handler for both form-based and OAuth2 logins.
 * This simplified version will be enhanced later to handle OAuth2 tokens once
 * the project dependencies are correctly set up.
 */
@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
    
    public CustomAuthenticationSuccessHandler() {
        setDefaultTargetUrl("/dashboard");
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        String email = authentication.getName();
        logger.info("User successfully authenticated: " + email);
        
        // Check if this is an OAuth2 authentication
        if (authentication.getClass().getName().contains("OAuth2")) {
            logger.info("OAuth2 authentication successful for user: " + email);
        } else {
            logger.info("Form-based authentication successful for user: " + email);
        }
        
        // Redirect to the success URL
        super.onAuthenticationSuccess(request, response, authentication);
    }
} 