package com.piaar.jwtsample.model.refresh_token.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import com.piaar.jwtsample.model.refresh_token.entity.RefreshTokenEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long>{
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM refresh_token WHERE refresh_token_pk NOT IN (SELECT tmp.* FROM (SELECT rt2.refresh_token_pk FROM refresh_token AS rt2 WHERE rt2.user_id=:userId ORDER BY rt2.created_at DESC LIMIT 2) AS tmp)", nativeQuery = true)
    void deleteOldRefreshTokens(String userId);

    @Query(value = "SELECT * FROM refresh_token AS rt WHERE rt.refresh_token_pk NOT IN (SELECT tmp.* FROM (SELECT rt2.refresh_token_pk FROM refresh_token rt2 WHERE rt2.user_id=:userId ORDER BY rt2.created_at DESC LIMIT 2) AS tmp)", nativeQuery = true)
    List<RefreshTokenEntity> test(String userId);

    Optional<RefreshTokenEntity> findByIdAndUserId(UUID rtId, UUID userId);

    Optional<RefreshTokenEntity> findById(UUID fromString);
}
