package com.piaar.jwtsample.domain.hello.controller;

import com.piaar.jwtsample.domain.hello.entity.HelloEntity;
import com.piaar.jwtsample.domain.hello.service.HelloService;
import com.piaar.jwtsample.domain.message.dto.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hello")
public class HelloApi {
    private final HelloService helloService;

    @Autowired
    public HelloApi(
            HelloService helloService
    ){
        this.helloService = helloService;
    }

    @GetMapping("")
    public ResponseEntity<?> helloGet(){
        Message message = new Message();
        message.setStatus(HttpStatus.OK);

        return new ResponseEntity<>(message, message.getStatus());
    }

    @PostMapping("")
    public ResponseEntity<?> helloPost(){
        Message message = new Message();

        HelloEntity entity = HelloEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();

        helloService.createHelloOne(entity);

        message.setStatus(HttpStatus.OK);

        return new ResponseEntity<>(message, message.getStatus());
    }
}
