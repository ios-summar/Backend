package com.summar.summar.service;

import com.summar.summar.domain.Follow;
import com.summar.summar.domain.RefreshToken;
import com.summar.summar.domain.User;
import com.summar.summar.dto.ChangeUserInfoRequestDto;
import com.summar.summar.dto.PushNotificationStatusDto;
import com.summar.summar.dto.RefreshTokenRequestDto;
import com.summar.summar.results.repository.FollowRepository;
import com.summar.summar.results.repository.RefreshTokenRepository;
import com.summar.summar.results.repository.UserRepository;
import com.summar.summar.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private FollowRepository followRepository;
    @Mock
    JwtUtil jwtUtil;


    static class TestUser extends User {
        public TestUser() {
            super();
        }
    }

    static class TestRefreshToken extends RefreshToken {
        public TestRefreshToken() {
            super();
        }
    }

    static class TestFollow extends Follow {
        public TestFollow() {
            super();
        }
    }


    @Test
    @DisplayName("유저 정보가 제대로 바뀌는 지 확인")
    void changeUserInfo() {
        //given
        User user = new TestUser();
        ReflectionTestUtils.setField(user, "userNickname", "beforeNickname");
        ReflectionTestUtils.setField(user, "major1", "beforemajor1");
        ReflectionTestUtils.setField(user, "major2", "beforemajor2");
        ReflectionTestUtils.setField(user, "profileImageUrl", "beforeImageUrl");
        ReflectionTestUtils.setField(user, "introduce", "beforeIntroduce");
        ReflectionTestUtils.setField(user, "leaveYn", false);


        given(userRepository.findByUserNicknameAndLeaveYn(any(), anyBoolean())).willReturn(Optional.of(user));

        ChangeUserInfoRequestDto changeUserInfoRequestDto = ChangeUserInfoRequestDto.builder().userNickname("beforeNickname").updateUserNickname("afterNickname").major1("afterMajor1").major2("afterMajor2").introduce("afterIntroduce").build();
        when(userRepository.save(any(User.class))).then(AdditionalAnswers.returnsFirstArg());
        //when
        userService.changeUserInfo(changeUserInfoRequestDto);

        //then
        assertEquals("afterNickname", user.getUserNickname());
    }

    @Test
    @DisplayName("리프레스 토큰이 유효할 때, 엑세스 토큰 정상적으로 재발급 되는지 확인")
    void giveAccessToken() {
        //given
        User user = new TestUser();
        ReflectionTestUtils.setField(user, "userSeq", 1L);
        ReflectionTestUtils.setField(user, "userEmail", "newy12@naver.com");

        RefreshToken refreshToken = new TestRefreshToken();
        ReflectionTestUtils.setField(refreshToken, "refreshToken", "abcdefg");
        given(userRepository.findByUserEmailAndLeaveYn(any(), anyBoolean())).willReturn(Optional.of(user));
        given(refreshTokenRepository.findByUser(any())).willReturn(Optional.of(refreshToken));
        given(jwtUtil.validateRefreshToken(anyString(), anyString())).willReturn(true);
        given(jwtUtil.generateToken(user)).willReturn("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMTE0ODk2NTE0MDAyMDExNTA4ODAiLCJleHAiOjE2NzY4NTAxMjIsImlhdCI6MTY3NjgxNDEyMn0.tNuTJCkABx_VUhvJlh4uAFPMUnsYaiX0j7YUN-xcGeM");

        RefreshTokenRequestDto refreshTokenRequestDto = new RefreshTokenRequestDto();
        refreshTokenRequestDto.setUserEmail("newy12@naver.com");

        //when
        userService.giveAccessToken(refreshTokenRequestDto);

        //then
        assertEquals("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMTE0ODk2NTE0MDAyMDExNTA4ODAiLCJleHAiOjE2NzY4NTAxMjIsImlhdCI6MTY3NjgxNDEyMn0.tNuTJCkABx_VUhvJlh4uAFPMUnsYaiX0j7YUN-xcGeM", userService.giveAccessToken(refreshTokenRequestDto));
    }

    @Test
    @DisplayName("리프레시 토큰, 엑세스 토큰 둘다 만료되었을 때, 둘 다 재발급 되는지 확인")
    void giveBothToken() {
        //given
        User user = new TestUser();
        RefreshToken refreshToken = new TestRefreshToken();
        RefreshTokenRequestDto refreshTokenRequestDto = new RefreshTokenRequestDto();
        ReflectionTestUtils.setField(refreshTokenRequestDto, "userEmail", "newy12@naver.com");
        given(userRepository.findByUserEmailAndLeaveYn(any(), anyBoolean())).willReturn(Optional.of(user));
        given(refreshTokenRepository.findByUser(any())).willReturn(Optional.of(refreshToken));
        given(jwtUtil.generateToken(user)).willReturn("accessToken");
        given(jwtUtil.generateRefreshToken(refreshTokenRequestDto.getUserEmail())).willReturn("refreshToken");
        when(refreshTokenRepository.save(any(RefreshToken.class))).then(AdditionalAnswers.returnsFirstArg());

        //when
        userService.giveBothToken(refreshTokenRequestDto);

        //then
        assertEquals("accessToken", userService.giveBothToken(refreshTokenRequestDto).getAccessToken());

        //타입 비교
        assertEquals("java.util.UUID", userService.giveBothToken(refreshTokenRequestDto).getRefreshTokenSeq().getClass().getName());
    }

    @Test
    @DisplayName("푸시알림 상태 제대로 변경 되는지 확인")
    void changePushNotification() {
        //given
        User user = new TestUser();
        PushNotificationStatusDto pushNotificationStatusDto = new PushNotificationStatusDto();
        given(userRepository.findByUserNicknameAndLeaveYn(any(), anyBoolean())).willReturn(Optional.of(user));
        ReflectionTestUtils.setField(user, "pushAlarmYn", false);
        ReflectionTestUtils.setField(pushNotificationStatusDto, "status", true);

        when(userRepository.save(any(User.class))).then(AdditionalAnswers.returnsFirstArg());
        //when
        userService.changePushNotification(pushNotificationStatusDto);

        //then
        assertTrue(user.getPushAlarmYn());
    }

    @Test
    @DisplayName("유저닉네임으로 푸시알림 상태 정상적으로 가져오는지 확인")
    void userPushStatusInfo() {
        //given
        User user = new TestUser();
        ReflectionTestUtils.setField(user, "pushAlarmYn", false);
        given(userRepository.findByUserSeqAndLeaveYn(any(), anyBoolean())).willReturn(Optional.of(user));

        //when
        userService.userPushStatusInfo(1L);

        //then
        assertFalse(userService.userPushStatusInfo(1L).getStatus());
    }

    @Test
    @DisplayName("회원 탈퇴 시 개인정보 초기화 제대로 되는지 확인")
    void leaveUser() {
        //given
        User user = new TestUser();
        ReflectionTestUtils.setField(user, "userSeq", 1L);
        given(userRepository.findByUserSeqAndLeaveYn(any(), anyBoolean())).willReturn(Optional.of(user));
        given(followRepository.findByFollowingUserAndFollowYn(any(), anyBoolean())).willReturn(Collections.emptyList());
        when(userRepository.save(any(User.class))).then(AdditionalAnswers.returnsFirstArg());


        //when
        userService.leaveUser(user.getUserSeq());

        //then
        assertEquals("", user.getUserNickname());
        assertEquals("", user.getUserEmail());
        assertEquals("", user.getDeviceToken());

    }
}
