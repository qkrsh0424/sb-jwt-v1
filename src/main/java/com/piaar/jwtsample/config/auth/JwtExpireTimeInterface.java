package com.piaar.jwtsample.config.auth;

public interface JwtExpireTimeInterface {
    final static Integer ACCESS_TOKEN_COOKIE_EXPIRATION = 24*60*60; // seconds | default: 20*60*1000, 24시간
    final static Integer ACCESS_TOKEN_JWT_EXPIRATION = 1*5*1000; // milliseconds | default: 20*60*1000, 20분
    final static Integer REFRESH_TOKEN_JWT_EXPIRATION = 24*60*60*1000; // milliseconds | default: 24*60*60*1000, 24시간
}
