package com.piaar.jwtsample.service.user;

import java.util.Optional;
import java.util.UUID;

import com.piaar.jwtsample.handler.DateHandler;
import com.piaar.jwtsample.model.user.dto.SignupReqDto;
import com.piaar.jwtsample.model.user.entity.UserEntity;
import com.piaar.jwtsample.model.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {
    @Autowired
    PasswordEncoder encoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DateHandler dateHandler;

    // Signup
    public String createOne(SignupReqDto signupReqDto){
        if(isDuplicatedUsername(signupReqDto.getUsername())){
            return "duplicated_username";
        }

        UserEntity userEntity = new UserEntity();
        String salt = UUID.randomUUID().toString();
        String password = encoder.encode(signupReqDto.getPassword() + salt);
        
        userEntity.setId(UUID.randomUUID());
        userEntity.setUsername(signupReqDto.getUsername());
        userEntity.setPassword(password);
        userEntity.setSalt(salt);
        userEntity.setRoles("ROLE_USER");
        userEntity.setUpdatedAt(dateHandler.getCurrentDate());
        userEntity.setCreatedAt(dateHandler.getCurrentDate());

        log.info("UserService : createOne : print(userEntity) => {}.", userEntity);
        userRepository.save(userEntity);
        return "success";
    }

    public boolean isDuplicatedUsername(String username){
        Optional<UserEntity> userEntityOpt = userRepository.findByUsername(username);
        if(userEntityOpt.isPresent()){
            return true;
        }
        
        return false;
    }
}
