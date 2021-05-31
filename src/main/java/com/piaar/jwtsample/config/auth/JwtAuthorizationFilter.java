package com.piaar.jwtsample.config.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.piaar.jwtsample.model.user.entity.UserEntity;
import com.piaar.jwtsample.model.user.repository.UserRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.util.WebUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private UserRepository userRepository;
    private String accessTokenSecret;
    private String refreshTokenSecret;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository,
            String accessTokenSecret, String refreshTokenSecret) {
        super(authenticationManager);
        // TODO Auto-generated constructor stub
        this.userRepository = userRepository;
        this.accessTokenSecret = accessTokenSecret;
        this.refreshTokenSecret = refreshTokenSecret;
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
        log.info("JwtAuthorizationFilter : doFilterInternal : print(accessToken) => {}", accessToken);

        Map<String, Object> accessTokenValify = valifyAccessToken(accessToken);
        if (accessTokenValify.get("message").equals("valified")) {
            Claims claims = (Claims) accessTokenValify.get("claims");
            System.out.println(claims.get("id"));
            System.out.println(claims.get("username"));
            System.out.println(claims.get("roles"));
            // TODO : 액세스 토큰이 정상적이라면 Authentication 을 발급해준다.
            UserEntity userEntity = new UserEntity();

            userEntity.setId(UUID.fromString(claims.get("id").toString()));
            userEntity.setUsername(claims.get("username").toString());
            userEntity.setRoles(claims.get("roles").toString());

            PrincipalDetails principalDetails = new PrincipalDetails(userEntity);

            // Jwt 토큰 서명을 통해서 서명이 정상이면 Authentication 객체를 만들어준다.
            Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());

            // 강제로 시큐리티의 세션에 접근하여 Authentication 객체를 저장.
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
        } else if (accessTokenValify.get("message").equals("expired")) {
            System.out.println(accessTokenValify.get("message"));
            Claims claims = (Claims) accessTokenValify.get("claims");
            System.out.println(claims.get("id"));
            System.out.println(claims.get("username"));
            System.out.println(claims.get("roles"));
            // TODO : 엑세스 토큰 만료되었을때 리프레시 토큰을 가져와서 비교후 엑세스 토큰과 리프레시 토큰을 재발급해준다.
        } else {

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
        }
        return result;
    }
}
