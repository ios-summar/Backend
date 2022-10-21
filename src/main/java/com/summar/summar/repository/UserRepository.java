package com.summar.summar.repository;

import com.summar.summar.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);

    boolean existsByUserNickname(String nickname);

    boolean existsByUserId(String userId);
}
