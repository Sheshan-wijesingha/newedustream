package com.example.LearningMedia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OAuth2Config {

    private final Environment env;

    public OAuth2Config(Environment env) {
        this.env = env;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();
        
        // Add Google registration
        ClientRegistration googleRegistration = googleClientRegistration();
        if (googleRegistration != null) {
            registrations.add(googleRegistration);
        }
        
        // Add Facebook registration
        ClientRegistration facebookRegistration = facebookClientRegistration();
        if (facebookRegistration != null) {
            registrations.add(facebookRegistration);
        }
        
        return new InMemoryClientRegistrationRepository(registrations);
    }

    private ClientRegistration googleClientRegistration() {
        String clientId = env.getProperty("spring.security.oauth2.client.registration.google.client-id");
        String clientSecret = env.getProperty("spring.security.oauth2.client.registration.google.client-secret");
        
        // For demo purposes, if no client ID is provided, use test values
        if (clientId == null || clientId.equals("YOUR_GOOGLE_CLIENT_ID")) {
            clientId = "demo-client-id";
            clientSecret = "demo-client-secret";
        }
        
        return ClientRegistration.withRegistrationId("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .clientName("Google")
                .build();
    }
    
    private ClientRegistration facebookClientRegistration() {
        String clientId = env.getProperty("spring.security.oauth2.client.registration.facebook.client-id");
        String clientSecret = env.getProperty("spring.security.oauth2.client.registration.facebook.client-secret");
        
        // Skip if demo credentials
        if (clientId == null || clientId.equals("DEMO-FB-APP-ID")) {
            return null;
        }
        
        return ClientRegistration.withRegistrationId("facebook")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("email", "public_profile")
                .authorizationUri("https://www.facebook.com/v12.0/dialog/oauth")
                .tokenUri("https://graph.facebook.com/v12.0/oauth/access_token")
                .userInfoUri("https://graph.facebook.com/v12.0/me?fields=id,name,email,picture")
                .userNameAttributeName("id")
                .clientName("Facebook")
                .build();
    }
} 