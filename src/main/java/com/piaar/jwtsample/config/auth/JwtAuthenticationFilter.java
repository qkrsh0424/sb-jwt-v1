package com.piaar.jwtsample.config.auth;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piaar.jwtsample.domain.message.dto.Message;
import com.piaar.jwtsample.model.refresh_token.entity.RefreshTokenEntity;
import com.piaar.jwtsample.model.refresh_token.repository.RefreshTokenRepository;
import com.piaar.jwtsample.model.user.entity.UserEntity;
import com.piaar.jwtsample.model.user.repository.UserRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private JwtTokenMaker jwtTokenMaker;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
            String accessTokenSecret, String refreshTokenSecret) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenMaker = new JwtTokenMaker(accessTokenSecret, refreshTokenSecret);

        setFilterProcessesUrl("/api/v1/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            // 1. request?????? username??? password??? ???????????? UserEntity ???????????? ???????????????.
            ObjectMapper om = new ObjectMapper();
            UserEntity userReqEntity = om.readValue(request.getInputStream(), UserEntity.class);

            // 2. request?????? ????????? username?????? DB??? ???????????? ???????????? ?????????????????? ????????? ????????????.
            // ????????? if ????????? BadCredentialsException ????????? ????????????.
            Optional<UserEntity> userEntityOpt = userRepository.findByUsername(userReqEntity.getUsername());
            if (!userEntityOpt.isPresent()) {
                throw new BadCredentialsException("username not founded");
            }
            // ????????? ?????? ?????? ???????????? ????????????.
            UserEntity userEntity = userEntityOpt.get();
            // request?????? ???????????? ??????????????? ???????????? ????????? ?????????????????? salt?????? ???????????? fullpassword??? ????????????.
            String fullPassword = userReqEntity.getPassword() + userEntity.getSalt();

            // 3. authentication??? ?????? ????????? ????????????.
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userReqEntity.getUsername(), fullPassword);
            // 4. authenticate??? ????????????.
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // ??????????????? return ????????? ???????????????, successfullAuthentication ???????????? ????????????,
            // ??????????????? unsuccessfulAuthentication ???????????? ????????????.
            return authentication;
        } catch (IOException e) {
            // ????????? ???????????? ????????? ??????????????? AuthenticationServiceException??? ??????
            // unsuccessfulAuthentication ???????????? ????????????.
            log.error("== ERROR JwtAuthenticationFilter => {}.==", "IOException");
            throw new AuthenticationServiceException("ioexception");
        }
    }

    // ================????????? ??????.==================
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        log.info("JwtAuthenticationFilter : successfulAuthentication : authResult => {}", authResult);
        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();

        // == REFRESH TOKEN ID ==
        UUID rtId = UUID.randomUUID();

        String accessToken = jwtTokenMaker.getAccessToken(principalDetails.getUser(), rtId);
        String refreshToken = jwtTokenMaker.getRefreshToken();

        // == ???????????? ?????? ?????? ==
        try{
            saveRefreshToken(principalDetails.getUser(), rtId, refreshToken);
        }catch(Exception e){
            log.error("JwtAuthenticationFilter : successfulAuthentication : saveRefreshToken => {}","DB save new refresh token error");
        }

        // == ???????????? ?????? ?????? ?????? ??????????????? ?????? ==
        try{
            deleteLimitRefreshToken(principalDetails.getUser());
        }catch(Exception e){
            log.error("JwtAuthenticationFilter : successfulAuthentication : deleteLimitRefreshToken => {}","DB delete old refresh token error");
        }


        ResponseCookie accessTokenCookie = ResponseCookie.from("piaar_actoken", accessToken).path("/")
                .httpOnly(true)
                .sameSite("Strict")
                // .secure(true)
                .maxAge(JwtExpireTimeInterface.ACCESS_TOKEN_COOKIE_EXPIRATION).build();

        Message message = new Message();
        message.setMessage("success");
        message.setStatus(HttpStatus.OK);

        ObjectMapper om = new ObjectMapper();
        String oms = om.writeValueAsString(message);

        response.setStatus(HttpStatus.OK.value());
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        response.getWriter().write(oms);
        response.getWriter().flush();
    }

    // ================????????? ??????.==================
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            org.springframework.security.core.AuthenticationException failed) throws IOException, ServletException {
        // response.sendError(HttpStatus.FORBIDDEN.value(), "message");

        Message message = new Message();
        message.setMessage("login_error");
        message.setStatus(HttpStatus.UNAUTHORIZED);
        message.setMemo("username not exist or password not matched.");

        ObjectMapper om = new ObjectMapper();
        String oms = om.writeValueAsString(message);

        response.setStatus(message.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        response.getWriter().write(oms);
        response.getWriter().flush();

    }

    private void saveRefreshToken(UserEntity userEntity, UUID rtId, String refreshToken){
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setId(rtId);
        refreshTokenEntity.setUserId(userEntity.getId());
        refreshTokenEntity.setRefreshToken(refreshToken);
        refreshTokenEntity.setCreatedAt(new Date());
        refreshTokenEntity.setUpdatedAt(new Date());
        refreshTokenRepository.save(refreshTokenEntity);
    }

    private void deleteLimitRefreshToken(UserEntity userEntity){
        refreshTokenRepository.deleteOldRefreshTokens(userEntity.getId().toString(), userEntity.getAllowedAccessCount());
    }
}
