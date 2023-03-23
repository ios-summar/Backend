package com.summar.summar.repository;

import com.summar.summar.domain.FeedScrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedScrapRepository extends JpaRepository<FeedScrap, Long> {

    Optional<FeedScrap> findByFeedFeedSeqAndUserUserSeq(Long feedSeq, Long userSeq);
    Page<FeedScrap> findByUserUserSeqAndActivatedIsTrueAndFeedActivatedIsTrueAndFeedUserLeaveYnIsFalse(Pageable page, Long userSeq);

    Optional<List<FeedScrap>> findByUserUserSeqAndActivatedIsTrueAndFeedActivatedIsTrueAndFeedUserUserSeq(Long userSeq, Long blockedUserSeq);

    boolean existsByActivatedIsTrueAndFeedFeedSeqAndUserUserSeq(Long feedSeq, Long userSeq);
}
