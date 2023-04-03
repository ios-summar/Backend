package com.summar.summar.results.repository;

import com.summar.summar.domain.Feed;
import com.summar.summar.domain.FeedImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedImageRepository extends JpaRepository<FeedImage, Long> {

    List<FeedImage> findByFeedSeqAndActivatedIsTrue(Long feedSeq);
}
