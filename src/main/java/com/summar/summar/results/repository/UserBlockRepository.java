package com.summar.summar.results.repository;

import com.summar.summar.domain.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    Optional<UserBlock> findByUserUserSeqAndBlockedUserUserSeq(Long userSeq,Long blockedUserSeq);

    boolean existsByUserUserSeqAndBlockedUserUserSeq(Long userSeq,Long blockedUserSeq);

    Optional<List<UserBlock>> findByUserUserSeq(Long userSeq);
}
