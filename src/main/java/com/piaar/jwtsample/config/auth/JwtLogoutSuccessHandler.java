package com.piaar.jwtsample.config.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piaar.jwtsample.domain.message.dto.Message;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

public class JwtLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        Message message = new Message();
        message.setMessage("success");
        message.setStatus(HttpStatus.OK);
        message.setMemo("logout success");

        ObjectMapper om = new ObjectMapper();
        String oms = om.writeValueAsString(message);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        response.getWriter().write(oms);
        response.getWriter().flush();

    }

}
