package com.summar.summar.results.repository;


import com.summar.summar.domain.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MajorRepository extends JpaRepository<Major, Long> {

    List<Major> findAllByParentsSeq(Long parentsSeq);

    List<Major> findAllByParentsSeqIsNull();

    Optional<Major> findByMajorName(String majorName);
}
