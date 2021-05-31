package com.piaar.jwtsample.config.auth;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piaar.jwtsample.model.message.Message;
import com.piaar.jwtsample.model.user.entity.UserEntity;
import com.piaar.jwtsample.model.user.repository.UserRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private String accessTokenSecret;
    private String refreshTokenSecret;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository,
            String accessTokenSecret, String refreshTokenSecret) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.accessTokenSecret = accessTokenSecret;
        this.refreshTokenSecret = refreshTokenSecret;
        setFilterProcessesUrl("/api/v1/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            // 1. request에서 username과 password를 받아와서 UserEntity 클래스로 매핑시킨다.
            ObjectMapper om = new ObjectMapper();
            UserEntity userReqEntity = om.readValue(request.getInputStream(), UserEntity.class);

            // 2. request에서 받아온 username으로 DB를 조회해서 일치하는 유저아이디가 있는지 확인한다.
            // 없다면 if 문에서 BadCredentialsException 예외를 타게한다.
            Optional<UserEntity> userEntityOpt = userRepository.findByUsername(userReqEntity.getUsername());
            if (!userEntityOpt.isPresent()) {
                throw new BadCredentialsException("username not founded");
            }
            // 있다면 해당 유저 데이터를 불러온다.
            UserEntity userEntity = userEntityOpt.get();
            // request에서 입력받은 패스워드와 디비에서 불러온 유저데이터중 salt값을 조합해서 fullpassword를 구성한다.
            String fullPassword = userReqEntity.getPassword() + userEntity.getSalt();

            // 3. authentication을 위한 토큰을 생성한다.
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userReqEntity.getUsername(), fullPassword);
            // 4. authenticate를 실행한다.
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 성공적이면 return 값으로 내보내주고, successfullAuthentication 핸들러로 점프하며,
            // 실패한다면 unsuccessfulAuthentication 핸들러로 점프한다.
            return authentication;
        } catch (IOException e) {
            // 인증중 알수없는 에러가 발생한다면 AuthenticationServiceException를 던져
            // unsuccessfulAuthentication 핸들러로 점프한다.
            log.error("== ERROR JwtAuthenticationFilter => {}.==", "IOException");
            throw new AuthenticationServiceException("ioexception");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        log.info("JwtAuthenticationFilter : successfulAuthentication : authResult => {}", authResult);
        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();

        // == REFRESH TOKEN CHECKER ==
        String rtc = UUID.randomUUID().toString();

        String accessToken = getAccessToken(principalDetails, rtc);
        String refreshToken = getRefreshToken(principalDetails, rtc);

        // == 리프레시 토큰 저장 ==
        userRepository.findById(principalDetails.getUser().getId()).ifPresent(r -> {
            r.setRefreshToken(refreshToken);
            userRepository.save(r);
        });

        ResponseCookie accessTokenCookie = ResponseCookie.from("piaar_actoken", accessToken).path("/").httpOnly(true)
                .sameSite("Strict")
                // .secure(true)
                .maxAge(60 * 60).build();

        Message message = new Message();
        message.setMessage("success");
        message.setStatus(HttpStatus.OK);

        ObjectMapper om = new ObjectMapper();
        String oms = om.writeValueAsString(message);

        response.setStatus(HttpStatus.OK.value());
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.getWriter().write(oms);
        response.getWriter().flush();
    }

    private String getAccessToken(PrincipalDetails principalDetails, String rtc) {
        // ===== Access Token Maker START =====
        // == ACCESS TOKEN Header ==
        Map<String, Object> atHeaders = new HashMap<>();
        atHeaders.put("typ", "JWT");
        atHeaders.put("alg", "HS256");

        // == ACCESS TOKEN Payload ==
        Map<String, Object> atPayloads = new HashMap<>();
        atPayloads.put("id", principalDetails.getUser().getId());
        atPayloads.put("username", principalDetails.getUser().getUsername());
        atPayloads.put("roles", principalDetails.getUser().getRoles());
        atPayloads.put("rtc", rtc);

        // == ACCESS TOKEN ==
        String accessToken = Jwts.builder().setHeader(atHeaders).setClaims(atPayloads).setSubject("JWT_ACT")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (10 * 60 * 1000))) // milliseconds
                .signWith(SignatureAlgorithm.HS256, accessTokenSecret.getBytes()).compact();
        log.info("JwtAuthentication : successfulAuthentication : print(accessToken) => {}", accessToken);
        // ===== Access Token Maker END =====
        return accessToken;
    }

    private String getRefreshToken(PrincipalDetails principalDetails, String rtc) {
        // ===== Refresh Token Maker START =====
        // == REFRESH TOKEN Header ==
        Map<String, Object> rtHeaders = new HashMap<>();
        rtHeaders.put("typ", "JWT");
        rtHeaders.put("alg", "HS256");

        // == REFRESH TOKEN Payload ==
        Map<String, Object> rtPayloads = new HashMap<>();
        rtPayloads.put("rtc", rtc);

        // == REFRESH TOKEN ==
        String refreshToken = Jwts.builder().setHeader(rtHeaders).setClaims(rtPayloads).setSubject("JWT_RFT")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (60 * 60 * 1000))) // milliseconds
                .signWith(SignatureAlgorithm.HS256, refreshTokenSecret.getBytes()).compact();
        log.info("JwtAuthentication : successfulAuthentication : print(refreshToken) => {}", refreshToken);
        // ===== Refresh Token Maker END =====
        return refreshToken;
    }
}
