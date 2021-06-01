package com.piaar.jwtsample.config.auth;

public interface JwtExpireTimeInterface {
    final static Integer ACCESS_TOKEN_COOKIE_EXPIRATION = 60*60; // seconds
    final static Integer ACCESS_TOKEN_JWT_EXPIRATION = 1*10*1000; // milliseconds
    final static Integer REFRESH_TOKEN_JWT_EXPIRATION = 24*60*60*1000; // milliseconds
}
