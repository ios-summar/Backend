package com.summar.summar.repository;


import com.summar.summar.config.TestConfig;
import com.summar.summar.domain.Follow;
import com.summar.summar.domain.User;
import com.summar.summar.helper.UserHelper;
import com.summar.summar.results.repository.FollowRepository;
import com.summar.summar.results.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
@Import(TestConfig.class)
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FollowRepository followRepository;


    @Test
    @DisplayName("닉네임으로 사용자 조회")
    void findUserByNickname() {
        //given
        User user = UserHelper.createDummyUser("tester1");
        userRepository.save(user);

        //when
        User findUser = userRepository.findByUserNicknameAndLeaveYn("tester1", false).orElse(null);

        //then
        assertEquals("tester1", Objects.requireNonNull(findUser).getUserNickname());
    }

    @Test
    @DisplayName("이메일로 회원 정보 찾기")
    void findUserByUserEmail() {
        //given
        User user = UserHelper.createDummyUser("tester2");
        userRepository.save(user);

        //when
        User findUser = userRepository.findByUserEmailAndLeaveYn("newy12@naver.com", false).orElse(null);

        //then
        assertEquals("tester2", Objects.requireNonNull(findUser).getUserNickname());
    }

    @Test
    @DisplayName("이미 존재하는 사용자 정보 찾기 - 이메일로")
    void existsByUserEmail() {
        //given
        User user = UserHelper.createDummyUser("tester3");

        //when
        userRepository.save(user);

        //then
        assertTrue(userRepository.existsByUserEmailAndLeaveYn("newy12@naver.com", false));
    }

    @Test
    @DisplayName("이미 존재하는 사용자 정보 찾기 - 닉네임으로")
    void existsByUserNickname() {
        //given
        User user = UserHelper.createDummyUser("tester4");

        //when
        userRepository.save(user);

        //then
        assertTrue(userRepository.existsByUserNicknameAndLeaveYn("tester4", false));
    }

    @Test
    @DisplayName("user 기본키로 사용자 정보 조회")
    void findByUserSeq() {
        //given
        User user = UserHelper.createDummyUser("tester5");
        userRepository.save(user);

        //when
        User findUser = userRepository.findById(user.getUserSeq()).orElse(null);

        //then
        assertEquals("tester5", Objects.requireNonNull(findUser).getUserNickname());

    }

    @Test
    @DisplayName("철자로 닉네임이 중복인지 확인")
    void existsByUserNicknameContains() {
        //given
        for (int i = 6; i < 10; i++) {
            User user = UserHelper.createDummyUser("tester" + i);
            //when
            userRepository.save(user);
        }
        //then
        assertTrue(userRepository.existsByUserNicknameContainsAndLeaveYn("t", false));
    }

    @Test
    @DisplayName("본인의 팔로워들의 정보")
    void findByFollowedUser() {
        //given
        for (int i = 1; i <= 2; i++) {
            User user = UserHelper.createDummyUser("tester" + i);
            userRepository.save(user);
        }
        User followedUser = userRepository.findByUserNicknameAndLeaveYn("tester1", false).orElse(null);
        User followingUser = userRepository.findByUserNicknameAndLeaveYn("tester2", false).orElse(null);
        Follow saveFollow = Follow.builder()
                .followYn(true)
                .followedUser(followedUser)
                .followingUser(followingUser)
                .followUp(false)
                .build();
        followRepository.save(saveFollow);
        //when
        Page<Follow> follow = followRepository.findByFollowedUserAndFollowYn(followedUser, true, Pageable.unpaged());
        Follow followInfo = follow.getContent().get(0);

        User findUser = userRepository.findById(followInfo.getFollowingUser().getUserSeq()).orElse(null);
        //then
        assertEquals("tester2", Objects.requireNonNull(findUser).getUserNickname());
    }

    @Test
    @DisplayName("본인이 팔로우 하고있는 유저의 정보")
    void findByFollowingUser() {
        //given
        for (int i = 3; i <= 4; i++) {
            User user = UserHelper.createDummyUser("tester" + i);
            userRepository.save(user);
        }
        User followedUser = userRepository.findByUserNicknameAndLeaveYn("tester3", false).orElse(null);
        User followingUser = userRepository.findByUserNicknameAndLeaveYn("tester4", false).orElse(null);
        Follow saveFollow = Follow.builder()
                .followYn(true)
                .followedUser(followedUser)
                .followingUser(followingUser)
                .followUp(false)
                .build();
        followRepository.save(saveFollow);

        //when
        Page<Follow> follow = followRepository.findByFollowingUserAndFollowYn(followingUser, true, Pageable.unpaged());
        Follow followInfo = follow.getContent().get(0);

        User findUser = userRepository.findById(followInfo.getFollowedUser().getUserSeq()).orElse(null);
        //then
        assertEquals("tester3", Objects.requireNonNull(findUser).getUserNickname());
    }

    @Test
    @DisplayName("본인계정이 팔로우 한 수(남의 계정을 팔로우할경우)")
    void countByFollowedUser() {
        //given
        for (int i = 11; i <= 15; i++) {
            User user = UserHelper.createDummyUser("tester" + i);
            userRepository.save(user);
        }

        User followedUser1 = userRepository.findByUserNicknameAndLeaveYn("tester5", false).orElse(null);
        User followedUser2 = userRepository.findByUserNicknameAndLeaveYn("tester6", false).orElse(null);
        User followedUser3 = userRepository.findByUserNicknameAndLeaveYn("tester7", false).orElse(null);
        User followingUser = userRepository.findByUserNicknameAndLeaveYn("tester8", false).orElse(null);

        Follow follow1 = Follow.builder()
                .followYn(true)
                .followedUser(followedUser1)
                .followingUser(followingUser)
                .followUp(false)
                .build();
        Follow follow2 = Follow.builder()
                .followYn(true)
                .followedUser(followedUser2)
                .followingUser(followingUser)
                .followUp(false)
                .build();
        Follow follow3 = Follow.builder()
                .followYn(true)
                .followedUser(followedUser3)
                .followingUser(followingUser)
                .followUp(false)
                .build();

        //then
        followRepository.save(follow1);
        followRepository.save(follow2);
        followRepository.save(follow3);

        //then
        assertEquals(3,followRepository.countByFollowingUserAndFollowYn(followingUser,true));
    }

    @Test
    @DisplayName("본인계정 팔로잉 수(남이 본인계정 팔로우할경우)")
    void countByFollowingUser() {
        //given
        for (int i = 5; i <= 10; i++) {
            User user = UserHelper.createDummyUser("tester" + i);
            userRepository.save(user);
        }

        User followedUser = userRepository.findByUserNicknameAndLeaveYn("tester5", false).orElse(null);
        User followingUser1 = userRepository.findByUserNicknameAndLeaveYn("tester6", false).orElse(null);
        User followingUser2 = userRepository.findByUserNicknameAndLeaveYn("tester7", false).orElse(null);
        User followingUser3 = userRepository.findByUserNicknameAndLeaveYn("tester8", false).orElse(null);

        Follow follow1 = Follow.builder()
                .followYn(true)
                .followedUser(followedUser)
                .followingUser(followingUser1)
                .followUp(false)
                .build();
        Follow follow2 = Follow.builder()
                .followYn(true)
                .followedUser(followedUser)
                .followingUser(followingUser2)
                .followUp(false)
                .build();
        Follow follow3 = Follow.builder()
                .followYn(true)
                .followedUser(followedUser)
                .followingUser(followingUser3)
                .followUp(false)
                .build();

        //when
        followRepository.save(follow1);
        followRepository.save(follow2);
        followRepository.save(follow3);

        //then
        assertEquals(3,followRepository.countByFollowedUserAndFollowYn(followedUser,true));
    }


}
