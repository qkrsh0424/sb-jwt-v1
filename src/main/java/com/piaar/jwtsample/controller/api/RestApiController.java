package com.piaar.jwtsample.controller.api;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.piaar.jwtsample.domain.message.dto.Message;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class RestApiController {
    @GetMapping("/test")
    public ResponseEntity<?> showTest(HttpServletRequest request){
        // log.info("RestApiController : showResource : print(authentication) => {}.", SecurityContextHolder.getContext().getAuthentication());
        Map<String, Object> data = new HashMap<>();
        data.put("msg", "test");
        Message message = new Message();
        message.setStatus(HttpStatus.OK);
        message.setData(data);
        return new ResponseEntity<>(message, message.getStatus());
    }

    @GetMapping("/resource")
    public ResponseEntity<?> showResource(HttpServletRequest request){
        // log.info("RestApiController : showResource : print(authentication) => {}.", SecurityContextHolder.getContext().getAuthentication());
        Map<String, Object> data = new HashMap<>();
        data.put("msg", "resource");

        Message message = new Message();
        message.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(message, message.getStatus());
    }

}
