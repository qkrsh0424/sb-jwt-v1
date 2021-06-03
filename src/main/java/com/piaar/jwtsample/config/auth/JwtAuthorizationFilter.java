package com.piaar.jwtsample.config.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.piaar.jwtsample.model.refresh_token.entity.RefreshTokenEntity;
import com.piaar.jwtsample.model.refresh_token.repository.RefreshTokenRepository;
import com.piaar.jwtsample.model.user.entity.UserEntity;
import com.piaar.jwtsample.model.user.repository.UserRepository;

import org.springframework.http.HttpHeaders;
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
        // 정상이라면 message:valified
        // 토큰이 만료이면 message:expired
        // 그외의 모든 에러는 message:error 로 처리된다.
        Map<String, Object> accessTokenValify = valifyAccessToken(accessToken);

        if (accessTokenValify.get("message").equals("valified")) { // 엑세스토큰 검증이 성공적이라면 실행.
            Claims claims = (Claims) accessTokenValify.get("claims");
            UserEntity userEntity = new UserEntity();

            userEntity.setId(UUID.fromString(claims.get("id").toString()));
            userEntity.setUsername(claims.get("username").toString());
            userEntity.setRoles(claims.get("roles").toString());

            // == Authentication Context 저장 ==
            saveAuthenticationContext(userEntity);
        } else if (accessTokenValify.get("message").equals("expired")) { // 엑세스토큰 검증이 만료된 토큰이라면 실행.
            Claims claims = (Claims) accessTokenValify.get("claims");

            /**
             * 1. 유저의 아이디값과, 엑세스 클레임 내에 있는 rtid 값을 이용해서 searchRefreshTokenEntity() 으로 디비의
             * 리프레시 토큰을 가져온다. 2. 불러온 리프레시 토큰을 이용하여 valifyRefreshToken() 를 실행하여 리프레시 토큰을
             * 검증한다. message:valified 라면 리프레시 토큰이 정상적이며, 그 이외의 값들은 일반적으로 에러로 처리되어 권한유지에 실패하게
             * 된다. 3. message:valified를 리턴받게되면, 새로운 액세스 토큰과 리프레시 토큰을 발급받게되며, 리프레시토큰은 디비로
             * 업데이트되고, 엑세스토큰은 클라이언트의 쿠키로 재저장 된다. 4. 마지막으로 권한 부여를 위하여
             * saveAuthenticationContext() 를 실행시켜 시큐리티에 authentication context를 저장한다.
             */
            RefreshTokenEntity refreshTokenEntity = searchRefreshTokenEntity(
                    UUID.fromString(claims.get("id").toString()), UUID.fromString(claims.get("rtid").toString()));
            if (refreshTokenEntity != null
                    && valifyRefreshToken(refreshTokenEntity).get("message").equals("valified")) {

                UserEntity userEntity = new UserEntity();
                userEntity.setId(UUID.fromString(claims.get("id").toString()));
                userEntity.setUsername(claims.get("username").toString());
                userEntity.setRoles(claims.get("roles").toString());

                String newAccessToken = jwtTokenMaker.getAccessToken(userEntity, refreshTokenEntity.getId());
                String newRefreshToken = jwtTokenMaker.getRefreshToken();

                // == 리프레시 토큰 생성 및 DB 업데이트 ==
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

    private Map<String, Object> valifyRefreshToken(RefreshTokenEntity refreshTokenEntity) {
        Map<String, Object> result = new HashMap<>();

        try {
            Jwts.parser().setSigningKey(refreshTokenSecret.getBytes())
                    .parseClaimsJws(refreshTokenEntity.getRefreshToken()).getBody();
            result.put("message", "valified");
        } catch (ExpiredJwtException e) {
            result.put("message", "expired");
        } catch (Exception e) {
            result.put("message", "error");
        }

        return result;
    }

    private RefreshTokenEntity searchRefreshTokenEntity(UUID userId, UUID rtId) {
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByIdAndUserId(rtId, userId).orElse(null);
        return refreshTokenEntity;
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
