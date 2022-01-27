package com.piaar.jwtsample.controller.api;

import com.piaar.jwtsample.domain.message.dto.Message;
import com.piaar.jwtsample.model.user.dto.SignupReqDto;
import com.piaar.jwtsample.service.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserApiController {
    @Autowired
    UserService userService;

    @GetMapping("/one")
    public ResponseEntity<?> searchOne() {
        Message message = new Message();
        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        return new ResponseEntity<>(message, message.getStatus());
    }

    @PostMapping("/one")
    public ResponseEntity<?> createOne(@RequestBody SignupReqDto signupReqDto) {
        Message message = new Message();
        log.info("UserApiController : createOne : print(SignupReqDto) => {}", signupReqDto);
        try {
            switch (userService.createOne(signupReqDto)) {
                case "success":
                    message.setStatus(HttpStatus.OK);
                    message.setMessage("success");
                    message.setMemo("sign up success.");
                    break;
                case "duplicated_username":
                    message.setStatus(HttpStatus.OK);
                    message.setMessage("duplicated_username");
                    message.setMemo("username is duplicated.");
                    break;
                case "failure":
                    message.setStatus(HttpStatus.OK);
                    message.setMessage("failure");
                    message.setMemo("failure");
                    break;
                default:
                    message.setStatus(HttpStatus.BAD_REQUEST);
                    message.setMessage("error");
                    break;
            }

        } catch (Exception e) {
            log.error("UserApiController : createOne => {}.", "create user error.");
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }
}
