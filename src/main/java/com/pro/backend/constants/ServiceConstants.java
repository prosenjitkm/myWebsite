package com.pro.backend.constants;

/**
 * Business / service-layer constants shared across services.
 * Keeps magic strings and numbers out of service logic.
 */
public final class ServiceConstants {

    private ServiceConstants() {}   // utility class — no instances

    // ----------------------------------------------------------------
    // Roles
    // ----------------------------------------------------------------
    public static final String ROLE_USER  = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    /** Prefix Spring Security expects on authority names */
    public static final String ROLE_PREFIX = "ROLE_";

    // ----------------------------------------------------------------
    // Error / exception messages
    // ----------------------------------------------------------------
    public static final String ERR_EMAIL_IN_USE        = "Email already in use.";
    public static final String ERR_USERNAME_TAKEN      = "Username already taken.";
    public static final String ERR_USER_ROLE_NOT_FOUND = "USER role not found in DB.";
    public static final String ERR_USER_NOT_FOUND      = "User not found.";
    public static final String ERR_POST_NOT_FOUND      = "Post not found.";
    public static final String ERR_COMMENT_NOT_FOUND   = "Parent comment not found.";

    // ----------------------------------------------------------------
    // Pagination defaults
    // ----------------------------------------------------------------
    public static final int    DEFAULT_PAGE_SIZE = 10;
    public static final String DEFAULT_POST_SORT = "publishedAt";

    // ----------------------------------------------------------------
    // JWT / token
    // ----------------------------------------------------------------
    public static final String BEARER_PREFIX         = "Bearer ";
    public static final String AUTH_HEADER           = "Authorization";
    public static final long   DEFAULT_JWT_EXPIRY_MS = 86_400_000L;   // 24 h

    // ----------------------------------------------------------------
    // OAuth2
    // ----------------------------------------------------------------
    public static final String OAUTH_PROVIDER_GOOGLE   = "google";
    public static final String OAUTH_ATTR_EMAIL        = "email";
    public static final String OAUTH_ATTR_GIVEN_NAME   = "given_name";
    public static final String OAUTH_ATTR_FAMILY_NAME  = "family_name";
    public static final String OAUTH_ATTR_SUB          = "sub";        // Google's unique user ID
    /** Query-param name used to carry the JWT back to the Angular SPA after OAuth2 login */
    public static final String OAUTH_TOKEN_PARAM       = "token";
}

