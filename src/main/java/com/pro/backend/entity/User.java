package com.pro.backend.entity;

import com.pro.backend.constants.FieldConstants;
import com.pro.backend.constants.ServiceConstants;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = FieldConstants.EMAIL_MAX)
    private String email;

    @Column(name = "password_hash", length = FieldConstants.PASSWORD_HASH_MAX)
    @JsonIgnore
    private String passwordHash;   // NULL for OAuth-only accounts

    @Column(nullable = false, unique = true, length = FieldConstants.USERNAME_MAX)
    private String username;

    @Column(name = "first_name", nullable = false, length = FieldConstants.FIRST_NAME_MAX)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = FieldConstants.LAST_NAME_MAX)
    private String lastName;

    /** e.g. "google", "github" — NULL means local email/password account */
    @Column(name = "oauth_provider", length = FieldConstants.OAUTH_PROVIDER_MAX)
    private String oauthProvider;

    /** The unique subject ID returned by the OAuth provider */
    @Column(name = "oauth_id", length = FieldConstants.OAUTH_ID_MAX)
    private String oauthId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = updatedAt = OffsetDateTime.now();
    }

    // ---- UserDetails ----

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(ServiceConstants.ROLE_PREFIX + role.getName()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;   // Spring Security uses email as the principal
    }

    @Override
    public boolean isAccountNonExpired()  { return true; }

    @Override
    public boolean isAccountNonLocked()   { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return isActive; }
}
