package com.piaar.jwtsample.config.auth;

import java.util.Optional;

import com.piaar.jwtsample.model.user.entity.UserEntity;
import com.piaar.jwtsample.model.user.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

// http://localhost:8081/login => 여기서 동작을 안한다.
@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService{

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO Auto-generated method stub
        Optional<UserEntity> userEntity = userRepository.findByUsername(username);
        return new PrincipalDetails(userEntity.get());
    }
    
}
