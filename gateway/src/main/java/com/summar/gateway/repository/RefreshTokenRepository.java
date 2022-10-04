package com.summar.gateway.repository;

import com.summar.gateway.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {


}
