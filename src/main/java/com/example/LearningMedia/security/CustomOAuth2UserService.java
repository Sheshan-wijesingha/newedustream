package com.example.LearningMedia.security;

import com.example.LearningMedia.model.User;
import com.example.LearningMedia.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to handle OAuth2 user authentication
 */
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private final UserService userService;
    private final DefaultOAuth2UserService delegate;

    @Autowired
    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
        this.delegate = new DefaultOAuth2UserService();
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            logger.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String email = extractEmail(provider, attributes);
        String name = extractName(provider, attributes);
        
        if (email != null) {
            // Save or update user in our database
            User user = userService.processOAuthPostLogin(email, name, provider);
            
            // Create a combined attributes map
            Map<String, Object> userAttributes = new HashMap<>(attributes);
            userAttributes.put("id", user.getId());
            userAttributes.put("email", email);
            userAttributes.put("name", name);
            userAttributes.put("provider", provider);
            
            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    userAttributes,
                    "email"
            );
        }
        
        throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
    }
    
    private String extractEmail(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("email");
        }
        
        return null;
    }
    
    private String extractName(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("name");
        }
        
        return "Unknown";
    }
} 