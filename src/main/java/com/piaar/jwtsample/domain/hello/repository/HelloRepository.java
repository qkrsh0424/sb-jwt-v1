package com.piaar.jwtsample.domain.hello.repository;

import com.piaar.jwtsample.domain.hello.entity.HelloEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HelloRepository extends JpaRepository<HelloEntity, Integer> {
}
