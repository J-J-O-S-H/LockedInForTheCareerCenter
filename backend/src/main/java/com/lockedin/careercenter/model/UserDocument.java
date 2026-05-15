package com.lockedin.careercenter.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class UserDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String googleId;

    private String displayName;

    private String photoUrl;

    private String authProvider;

    private Instant createdAt;

    public UserDocument() {
    }

    public UserDocument(String email, String password, Instant createdAt) {
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.authProvider = "email";
    }

    public UserDocument(String email, String googleId, String displayName, String photoUrl, Instant createdAt) {
        this.email = email;
        this.googleId = googleId;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.createdAt = createdAt;
        this.authProvider = "google";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
