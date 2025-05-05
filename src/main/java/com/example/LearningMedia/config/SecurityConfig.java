package com.example.LearningMedia.config;

import com.example.LearningMedia.security.CustomUserDetailsService;
import com.example.LearningMedia.security.CustomAuthenticationSuccessHandler;
import com.example.LearningMedia.security.SimplifiedOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.env.Environment;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final Environment env;
    
    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          CustomAuthenticationSuccessHandler successHandler,
                          Environment env) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
        this.env = env;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Disable CSRF for REST APIs for demo purposes
        http.csrf(csrf -> csrf.disable());
        
        // Authentication and authorization
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/register", "/api/login", "/oauth2/**", "/login/**", "/register", "/").permitAll()
            .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
            .requestMatchers("/api/user/me").authenticated()
            .requestMatchers("/api/check-email").permitAll()
            .anyRequest().authenticated()
        );
        
        // Form login 
        http.formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/dashboard")
            .successHandler(successHandler)
            .permitAll()
        );
        
        // OAuth2 login configuration supports Google and Facebook
        http.oauth2Login(oauth2 -> oauth2
            .loginPage("/login")
            .successHandler(successHandler)
        );
        
        // Logout configuration
        http.logout(logout -> logout
            .logoutSuccessUrl("/login?logout")
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID")
            .permitAll()
        );
        
        return http.build();
    }
} 