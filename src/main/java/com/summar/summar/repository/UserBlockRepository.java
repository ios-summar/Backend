package com.summar.summar.repository;

import com.summar.summar.domain.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    Optional<UserBlock> findByUserUserSeqAndBlockedUserUserSeq(Long userSeq,Long blockedUserSeq);
}
