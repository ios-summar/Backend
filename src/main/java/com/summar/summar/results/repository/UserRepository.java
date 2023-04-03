package com.summar.summar.results.repository;

import com.summar.summar.domain.User;
import com.summar.summar.enumeration.SocialType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{


    boolean existsByUserNicknameAndLeaveYn(String nickname,boolean leaveYn);
    boolean existsByUserNicknameContainsAndLeaveYn(String nickname,boolean leaveYn);

    Optional<User> findByUserEmailAndLeaveYn(String userEmail, boolean leaveYn);

    boolean existsByUserEmailAndLeaveYn(String userEmail,boolean leaveYn);

    Optional<User> findByUserNicknameAndLeaveYn(String userNickname,boolean leaveYn);

    Page<User> findByUserNicknameContainsAndLeaveYnAndUserSeqNotIn(String userNickname,boolean leaveYn ,Pageable pageable, List<Long> blockedUserSeqs);

    Page<User> findByUserNicknameContainsAndLeaveYn(String userNickname,boolean leaveYn ,Pageable pageable);

    @Query("select u from User u where u.userNickname >=:word and u.userNickname <:word2 and u.leaveYn = false and u.userSeq not in :blockedUserSeqs")
    Page<User> searchWord(String word,String word2,Pageable pageable, List<Long> blockedUserSeqs);

    @Query("select u from User u where u.userNickname >=:word and u.userNickname <:word2 and u.leaveYn = false")
    Page<User> searchWord(String word,String word2,Pageable pageable);

    Optional<User> findByUserSeqAndLeaveYn(Long userSeq, boolean leaveYn);

    List<User> findAllByLeaveYn(boolean leaveYn);
}
