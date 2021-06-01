package com.piaar.jwtsample.controller.api;

import com.piaar.jwtsample.model.message.Message;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class RestApiController {
    @GetMapping("/resource")
    public ResponseEntity<?> showResource(){
        // log.info("RestApiController : showResource : print(authentication) => {}.", SecurityContextHolder.getContext().getAuthentication());
        Message message = new Message();
        message.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(message, message.getStatus());
    }
}
