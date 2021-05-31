package com.piaar.jwtsample.model.user.dto;

import lombok.Data;

@Data
public class LoginReqDto {
    private String username;
    private String password;
}
