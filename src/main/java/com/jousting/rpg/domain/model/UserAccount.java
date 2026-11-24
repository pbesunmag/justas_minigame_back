package com.jousting.rpg.domain.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Representa la cuenta de un usuario jugador e implementa UserDetails de Spring Security.
 */
public class UserAccount implements UserDetails {

    private Long id;
    private final String username;
    private String password; // Almacenada siempre encriptada con BCrypt
    private final String role;
    private final LocalDateTime createdAt;

    public UserAccount(Long id, String username, String encodedPassword, String role) {
        this.id = id;
        this.username = username;
        this.password = encodedPassword;
        this.role = (role != null) ? role : "ROLE_USER";
        this.createdAt = LocalDateTime.now();
    }

    public UserAccount(String username, String encodedPassword) {
        this(null, username, encodedPassword, "ROLE_USER");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
