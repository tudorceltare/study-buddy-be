package com.ps.studybuddy.security.constant;

public class SecurityConstant {
    public static final long EXPIRATION_TIME = 432_000_000;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
    public static final String ISSUER = "ps";
    public static final String AUDIENCE = "Study Buddy Portal";
    public static final String AUTHORITIES = "Authorities";
    public static final String FORBIDDEN_MESSAGE = "Login to access this page";
    public static final String ACCESS_DENIED_MESSAGE = "You do not have permission to access this page";
    public static final String OPTIONS_HTTP_METHOD = "OPTIONS";
    public static final String LOCAL_HOST = "http://localhost:4200";
    //    public static final String[] PUBLIC_URLS = { "/login", "/person/register", "/person/reset-password/**"};
    public static final String[] PUBLIC_URLS = {"**"};
}
