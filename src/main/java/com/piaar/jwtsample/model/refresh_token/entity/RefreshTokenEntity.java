package com.piaar.jwtsample.model.refresh_token.entity;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.Data;

@Entity
@Data
@Table(name = "refresh_token")
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_pk")
    private Long refreshTokenPk;

    @Type(type = "uuid-char")
    @Column(name = "id")
    private UUID id;

    @Type(type = "uuid-char")
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;


}
