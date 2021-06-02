package com.piaar.jwtsample.config.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piaar.jwtsample.model.message.Message;
import com.piaar.jwtsample.model.refresh_token.entity.RefreshTokenEntity;
import com.piaar.jwtsample.model.refresh_token.repository.RefreshTokenRepository;
import com.piaar.jwtsample.model.user.entity.UserEntity;
import com.piaar.jwtsample.model.user.repository.UserRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.util.WebUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private String accessTokenSecret;
    private String refreshTokenSecret;
    private JwtTokenMaker jwtTokenMaker;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository, String accessTokenSecret, String refreshTokenSecret) {
        super(authenticationManager);
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessTokenSecret = accessTokenSecret;
        this.refreshTokenSecret = refreshTokenSecret;
        this.jwtTokenMaker = new JwtTokenMaker(accessTokenSecret, refreshTokenSecret);
    }

    // TODO : 코드 리펙터링
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // == GET ACCESS TOKEN COOKIE ==
        Cookie jwtTokenCookie = WebUtils.getCookie(request, "piaar_actoken");

        // 액세스 토큰 쿠키가 있는지 확인, 만약에 없다면 체인을 계속 타게하고 있다면 검증한다.
        if (jwtTokenCookie == null) {
            chain.doFilter(request, response);
            return;
        }

        // == GET ACCESS TOKEN VALUE ==
        String accessToken = jwtTokenCookie.getValue();

        // 엑세스 토큰이 유효한지 검사한다.
        Map<String, Object> accessTokenValify = valifyAccessToken(accessToken);
        if (accessTokenValify.get("message").equals("valified")) {
            Claims claims = (Claims) accessTokenValify.get("claims");
            UserEntity userEntity = new UserEntity();

            userEntity.setId(UUID.fromString(claims.get("id").toString()));
            userEntity.setUsername(claims.get("username").toString());
            userEntity.setRoles(claims.get("roles").toString());

            // == Authentication Context 저장 ==
            saveAuthenticationContext(userEntity);

        } else if (accessTokenValify.get("message").equals("expired")) {
            Claims claims = (Claims) accessTokenValify.get("claims");

            /**
             * 1. getRefreshToken() 으로 디비의 리프레시 토큰을 가져온다. 2. 리프레시 토큰과, 엑세스토큰의 rtc를 변수로
             * valifyRefreshToken()에 넣어서 리프레시 토큰의 유효성과 rtc의 일치성을 확인한다. \ 3. 유효한 rtc값을 가지며,
             * 리프레시 토큰이 유효하다면 {message : valified} 를 리턴해준다. 4. 검사 결과가 valified이면,
             * accessToken과 refreshToken을 재생성해서 액세스 토큰은 쿠키로, 리프레시토큰은 디비로 저장한다.
             */
            RefreshTokenEntity refreshTokenEntity = getRefreshToken(UUID.fromString(claims.get("id").toString()), UUID.fromString(claims.get("rtid").toString()));
            if (refreshTokenEntity!=null && valifyRefreshToken(refreshTokenEntity).get("message").equals("valified")) {

                UserEntity userEntity = new UserEntity();
                userEntity.setId(UUID.fromString(claims.get("id").toString()));
                userEntity.setUsername(claims.get("username").toString());
                userEntity.setRoles(claims.get("roles").toString());

                String newAccessToken = jwtTokenMaker.getAccessToken(userEntity, refreshTokenEntity.getId());
                String newRefreshToken = jwtTokenMaker.getRefreshToken(userEntity, refreshTokenEntity.getId());

                // == 리프레시 토큰 생성 및 DB 저장 ==
                refreshTokenRepository.findById(refreshTokenEntity.getId()).ifPresent(r -> {
                    r.setRefreshToken(newRefreshToken);
                    refreshTokenRepository.save(r);
                });

                // == 엑세스 토큰 쿠키 생성 및 저장 ==
                ResponseCookie accessTokenCookie = ResponseCookie.from("piaar_actoken", newAccessToken).path("/")
                        .httpOnly(true).sameSite("Strict")
                        // .secure(true)
                        .maxAge(JwtExpireTimeInterface.ACCESS_TOKEN_COOKIE_EXPIRATION).build();
                response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

                // == Authentication Context 저장 ==
                saveAuthenticationContext(userEntity);
            }
        } else {
            chain.doFilter(request, response);
            return;
        }

        chain.doFilter(request, response);
    }

    private Map<String, Object> valifyAccessToken(String accessToken) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("message", "valified");
            result.put("claims",
                    Jwts.parser().setSigningKey(accessTokenSecret.getBytes()).parseClaimsJws(accessToken).getBody());
        } catch (ExpiredJwtException e) {
            result.put("message", "expired");
            result.put("claims", e.getClaims());
        } catch (Exception e) {
            result.put("message", "error");
            result.put("claims", null);
        }
        return result;
    }

    private RefreshTokenEntity getRefreshToken(UUID userId, UUID rtId) {
        // UserEntity userEntity =
        // userRepository.findById(UUID.fromString(userId.toString())).orElse(null);
        // return userEntity.getRefreshToken();
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByIdAndUserId(rtId, userId).orElse(null);
        return refreshTokenEntity;
    }

    private Map<String, Object> valifyRefreshToken(RefreshTokenEntity refreshTokenEntity) {
        Map<String, Object> result = new HashMap<>();

        try {
            Claims claims = Jwts.parser().setSigningKey(refreshTokenSecret.getBytes()).parseClaimsJws(refreshTokenEntity.getRefreshToken())
                    .getBody();
            result.put("message", "valified");
        } catch (ExpiredJwtException e) {
            result.put("message", "expired");
        } catch (Exception e) {
            result.put("message", "error");
        }

        return result;
    }

    private void saveAuthenticationContext(UserEntity userEntity) {
        PrincipalDetails principalDetails = new PrincipalDetails(userEntity);

        // Jwt 토큰 서명을 통해서 서명이 정상이면 Authentication 객체를 만들어준다.
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null,
                principalDetails.getAuthorities());

        // 강제로 시큐리티의 세션에 접근하여 Authentication 객체를 저장.
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
