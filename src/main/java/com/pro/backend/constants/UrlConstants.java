package com.pro.backend.constants;

/**
 * Central registry of every API URL path in the application.
 * Each nested class groups paths by controller.
 * Change a value here and it propagates everywhere automatically.
 */
public final class UrlConstants {

    private UrlConstants() {}   // utility class — no instances

    // ----------------------------------------------------------------
    // Base prefix
    // ----------------------------------------------------------------
    public static final String API_BASE = "/api";

    // ----------------------------------------------------------------
    // Auth  →  /api/auth/**
    // ----------------------------------------------------------------
    public static final class Auth {
        private Auth() { throw new UnsupportedOperationException(); }

        public static final String BASE     = API_BASE + "/auth";
        public static final String REGISTER = "/register";
        public static final String LOGIN    = "/login";
        public static final String WILDCARD = BASE + "/**";
    }

    // ----------------------------------------------------------------
    // Posts  →  /api/posts/**
    // ----------------------------------------------------------------
    public static final class Posts {
        private Posts() { throw new UnsupportedOperationException(); }

        public static final String BASE     = API_BASE + "/posts";
        public static final String BY_SLUG  = "/{slug}";
        public static final String COMMENTS = "/{postId}/comments";
        public static final String WILDCARD = BASE + "/**";
    }

    // ----------------------------------------------------------------
    // Resume  →  /api/resume/**
    // ----------------------------------------------------------------
    public static final class Resume {
        private Resume() { throw new UnsupportedOperationException(); }

        public static final String BASE     = API_BASE + "/resume";
        public static final String WILDCARD = BASE + "/**";
    }

    // ----------------------------------------------------------------
    // OAuth2  →  Spring Security's built-in OAuth2 endpoints +
    //            our custom callback redirect to the Angular SPA
    // ----------------------------------------------------------------
    public static final class OAuth2 {
        private OAuth2() { throw new UnsupportedOperationException(); }

        /** Spring Security handles login initiation at this path automatically */
        public static final String LOGIN_BASE     = "/oauth2/authorization";
        /** Spring Security's built-in redirect receiver (must match Google Console setting) */
        public static final String CALLBACK_BASE  = "/login/oauth2/code";
        /** Wildcard used in SecurityConfig to permit the callback */
        public static final String CALLBACK_WILDCARD = CALLBACK_BASE + "/**";
        /** Where the success handler redirects the Angular SPA (with ?token=…) */
        public static final String FRONTEND_REDIRECT = "http://localhost:4200/oauth2/callback";
    }
}
