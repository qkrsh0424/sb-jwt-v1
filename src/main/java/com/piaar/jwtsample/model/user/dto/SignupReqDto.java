package com.piaar.jwtsample.model.user.dto;

import lombok.Data;

@Data
public class SignupReqDto {
    private String username;
    private String password;
}
