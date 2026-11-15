package com.conectaedu.api.modules.auth.repository;

import com.conectaedu.api.modules.auth.domain.RevokedToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<RevokedToken, UUID> {

    boolean existsByTokenId(String tokenId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM RevokedToken token
        WHERE token.expiresAt < :now
    """)
    void deleteExpiredTokens(@Param("now") Instant now);
}
