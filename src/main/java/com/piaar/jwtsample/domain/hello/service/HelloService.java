package com.piaar.jwtsample.domain.hello.service;

import com.piaar.jwtsample.domain.hello.entity.HelloEntity;
import com.piaar.jwtsample.domain.hello.repository.HelloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HelloService {
    private final HelloRepository helloRepository;

    @Autowired
    public HelloService(
            HelloRepository helloRepository
    ){
        this.helloRepository = helloRepository;
    }
    public void createHelloOne(HelloEntity entity){
        helloRepository.save(entity);
    }
}
