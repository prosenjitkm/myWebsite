package com.pro.backend.security;

import com.pro.backend.constants.FieldConstants;
import com.pro.backend.constants.ServiceConstants;
import com.pro.backend.constants.UrlConstants;
import com.pro.backend.entity.Role;
import com.pro.backend.entity.User;
import com.pro.backend.repository.RoleRepository;
import com.pro.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

/**
 * Invoked by Spring Security after a successful OAuth2 login (e.g. Google).
 *
 * Flow:
 *  1. Extract the user's email + profile from the OAuth2 token.
 *  2. Find an existing account by (provider, oauthId) — or create one on first login.
 *  3. Issue our own JWT and redirect the Angular SPA to /oauth2/callback?token=<jwt>
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email      = oAuth2User.getAttribute(ServiceConstants.OAUTH_ATTR_EMAIL);
        String oauthId    = oAuth2User.getAttribute(ServiceConstants.OAUTH_ATTR_SUB);
        String firstName  = oAuth2User.getAttribute(ServiceConstants.OAUTH_ATTR_GIVEN_NAME);
        String lastName   = oAuth2User.getAttribute(ServiceConstants.OAUTH_ATTR_FAMILY_NAME);
        String provider   = ServiceConstants.OAUTH_PROVIDER_GOOGLE;

        // Find by (provider, oauthId) first — most reliable approach
        User user = userRepository
                .findByOauthProviderAndOauthId(provider, oauthId)
                .orElseGet(() -> findOrCreateByEmail(email, oauthId, provider, firstName, lastName));

        String jwt = jwtService.generateToken(user);

        // Redirect to the Angular SPA with the JWT as a query param
        String redirectUrl = UriComponentsBuilder
                .fromUriString(UrlConstants.OAuth2.FRONTEND_REDIRECT)
                .queryParam(ServiceConstants.OAUTH_TOKEN_PARAM, jwt)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    /**
     * Falls back to looking up by email (handles the case where a user
     * registered with email+password first, then tries Google with the
     * same address — we link the accounts automatically).
     */
    private User findOrCreateByEmail(String email, String oauthId, String provider,
                                     String firstName, String lastName) {
        return userRepository.findByEmail(email)
                .map(existing -> {
                    // Link the OAuth identity to the existing account
                    existing.setOauthProvider(provider);
                    existing.setOauthId(oauthId);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> createNewOAuthUser(email, oauthId, provider, firstName, lastName));
    }

    private User createNewOAuthUser(String email, String oauthId, String provider,
                                    String firstName, String lastName) {
        Role userRole = roleRepository.findByName(ServiceConstants.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException(ServiceConstants.ERR_USER_ROLE_NOT_FOUND));

        // Generate a unique username from the email local-part
        String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
        String username = uniqueUsername(baseUsername);

        User newUser = User.builder()
                .email(email)
                .passwordHash(null)          // no password — OAuth-only account
                .username(username)
                .firstName(firstName != null ? firstName : "")
                .lastName(lastName  != null ? lastName  : "")
                .oauthProvider(provider)
                .oauthId(oauthId)
                .role(userRole)
                .isActive(true)
                .build();

        return userRepository.save(newUser);
    }

    /** Appends a numeric suffix until the username is unique */
    private String uniqueUsername(String base) {
        // Trim to fit the column max length, leaving room for a suffix
        String trimmed = base.length() > FieldConstants.USERNAME_MAX - 4
                ? base.substring(0, FieldConstants.USERNAME_MAX - 4)
                : base;

        if (!userRepository.existsByUsername(trimmed)) return trimmed;

        int suffix = 1;
        while (userRepository.existsByUsername(trimmed + suffix)) suffix++;
        return trimmed + suffix;
    }
}

