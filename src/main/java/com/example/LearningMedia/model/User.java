package com.example.LearningMedia.model;

import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Indexed(unique = true)
    private String email;
    
    @Indexed(unique = true)
    private String username;
    
    private String password;
    
    private boolean enabled = true;
    
    private Set<String> authorities = new HashSet<>();
    
    private Set<String> oauthProviders = new HashSet<>();
    
    private String profilePicture;
    
    private String bio;
    
    private List<String> skills = new ArrayList<>();
    
    @Transient
    private boolean currentUserFollowing;
    
    // Default constructor
    public User() {
    }
    
    // Constructor with required fields
    public User(String firstName, String lastName, LocalDate dateOfBirth, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.password = password;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Set<String> getAuthorities() {
        return authorities;
    }
    
    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }
    
    public Set<String> getOauthProviders() {
        return oauthProviders;
    }
    
    public void setOauthProviders(Set<String> oauthProviders) {
        this.oauthProviders = oauthProviders;
    }
    
    public String getProfilePicture() {
        return profilePicture;
    }
    
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public List<String> getSkills() {
        return skills;
    }
    
    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
    
    public void addSkill(String skill) {
        if (this.skills == null) {
            this.skills = new ArrayList<>();
        }
        this.skills.add(skill);
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // Method to verify if the user is at least 15 years old
    public boolean isAtLeast15YearsOld() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears() >= 15;
    }
    
    // Add an OAuth provider to the user
    public void addOAuthProvider(String provider) {
        this.oauthProviders.add(provider);
    }
    
    // Add an authority to the user
    public void addAuthority(String authority) {
        this.authorities.add(authority);
    }
    
    // Get and set if the current user is following this user
    public boolean isCurrentUserFollowing() {
        return currentUserFollowing;
    }
    
    public void setCurrentUserFollowing(boolean currentUserFollowing) {
        this.currentUserFollowing = currentUserFollowing;
    }
} 