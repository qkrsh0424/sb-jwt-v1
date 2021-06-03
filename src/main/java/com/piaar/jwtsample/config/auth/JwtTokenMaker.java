package com.piaar.jwtsample.config.auth;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.piaar.jwtsample.model.user.entity.UserEntity;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtTokenMaker {
    private String accessTokenSecret;
    private String refreshTokenSecret;

    public JwtTokenMaker(String accessTokenSecret, String refreshTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
        this.refreshTokenSecret = refreshTokenSecret;
    }


    /**
     * 유저 데이터와 생성된 리프레시토큰 아이디를 가지고 엑세스 토큰을 만든다.
     * @param userEntity
     * @param rtId
     * @return accessToken : String
     * @author SHP Austine
     */
    public String getAccessToken(UserEntity userEntity, UUID rtId) {
        // ===== Access Token Maker START =====
        // == ACCESS TOKEN Header ==
        Map<String, Object> atHeaders = new HashMap<>();
        atHeaders.put("typ", "JWT");
        atHeaders.put("alg", "HS256");

        // == ACCESS TOKEN Payload ==
        Map<String, Object> atPayloads = new HashMap<>();
        atPayloads.put("id", userEntity.getId());
        atPayloads.put("username", userEntity.getUsername());
        atPayloads.put("roles", userEntity.getRoles());
        atPayloads.put("rtid", rtId);

        // == ACCESS TOKEN ==
        String accessToken = Jwts.builder().setHeader(atHeaders).setClaims(atPayloads).setSubject("JWT_ACT")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (JwtExpireTimeInterface.ACCESS_TOKEN_JWT_EXPIRATION))) // milliseconds
                .signWith(SignatureAlgorithm.HS256, accessTokenSecret.getBytes()).compact();
        // ===== Access Token Maker END =====
        return accessToken;
    }

    /**
     * 리프레시 토큰을 만든다.
     * @return refreshToken : String
     * @author SHP Austine
     */
    public String getRefreshToken() {
        // ===== Refresh Token Maker START =====
        // == REFRESH TOKEN Header ==
        Map<String, Object> rtHeaders = new HashMap<>();
        rtHeaders.put("typ", "JWT");
        rtHeaders.put("alg", "HS256");

        // == REFRESH TOKEN Payload ==
        Map<String, Object> rtPayloads = new HashMap<>();

        // == REFRESH TOKEN ==
        String refreshToken = Jwts.builder().setHeader(rtHeaders).setClaims(rtPayloads).setSubject("JWT_RFT")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (JwtExpireTimeInterface.REFRESH_TOKEN_JWT_EXPIRATION))) // milliseconds
                .signWith(SignatureAlgorithm.HS256, refreshTokenSecret.getBytes()).compact();
        // ===== Refresh Token Maker END =====
        return refreshToken;
    }
}
