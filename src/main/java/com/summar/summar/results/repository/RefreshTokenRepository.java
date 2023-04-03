package com.summar.summar.results.repository;

import com.summar.summar.domain.RefreshToken;
import com.summar.summar.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByUserAndRefreshToken(User user, String refreshToken);

    Optional<RefreshToken> findByRefreshTokenSeq(UUID refreshTokenId);

    Optional<RefreshToken> deleteByRefreshTokenSeq(UUID refreshTokenSeq);

    Optional<RefreshToken> findByUser(User user);

}
