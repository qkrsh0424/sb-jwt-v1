package com.piaar.jwtsample.model.refresh_token.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import com.piaar.jwtsample.model.refresh_token.entity.RefreshTokenEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long>{
    /**
     * deleteOldRefreshTokens
     * 설명 : 먼저 리프레시 유저 아이디에 해당하는 리프레시 토큰을 최신순으로 N개를 얻어온다. 그리고 얻어온 N개를 제외한 유저아이디에 해당하는 나머지 데이터를 모두 삭제한다.
     * @param userId
     * @param allowedAccessCount
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM refresh_token WHERE user_id=:userId AND refresh_token_pk NOT IN (SELECT tmp.* FROM (SELECT rt2.refresh_token_pk FROM refresh_token AS rt2 WHERE rt2.user_id=:userId ORDER BY rt2.created_at DESC LIMIT :allowedAccessCount) AS tmp)", nativeQuery = true)
    void deleteOldRefreshTokens(String userId, Integer allowedAccessCount);

    @Query(value = "SELECT * FROM refresh_token AS rt WHERE rt.refresh_token_pk NOT IN (SELECT tmp.* FROM (SELECT rt2.refresh_token_pk FROM refresh_token rt2 WHERE rt2.user_id=:userId ORDER BY rt2.created_at DESC LIMIT 2) AS tmp)", nativeQuery = true)
    List<RefreshTokenEntity> test(String userId);

    Optional<RefreshTokenEntity> findByIdAndUserId(UUID rtId, UUID userId);

    Optional<RefreshTokenEntity> findById(UUID fromString);
}
