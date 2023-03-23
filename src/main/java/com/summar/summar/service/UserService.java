package com.summar.summar.service;

import com.summar.summar.common.SummarCommonException;
import com.summar.summar.common.SummarErrorCode;
import com.summar.summar.domain.Follow;
import com.summar.summar.domain.RefreshToken;
import com.summar.summar.domain.User;
import com.summar.summar.domain.UserBlock;
import com.summar.summar.dto.*;
import com.summar.summar.repository.*;
import com.summar.summar.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FollowRepository followRepository;
    private final GatheringNotificationRepository gatheringNotificationRepository;
    private final UserBlockRepository userBlockRepository;
    private final JwtUtil jwtUtil;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public Boolean checkNicknameDuplication(String nickname) throws NoSuchAlgorithmException {
        return userRepository.existsByUserNicknameAndLeaveYn(nickname, false);
    }

    @Transactional
    public void changeUserInfo(ChangeUserInfoRequestDto changeUserInfoRequestDto) {
        User user = userRepository.findByUserNicknameAndLeaveYn(changeUserInfoRequestDto.getUserNickname(), false).orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        //s3 저장
        ChangeUserInfoResponseDto changeUserInfoResponseDto = new ChangeUserInfoResponseDto();
        if (changeUserInfoRequestDto.getFile() != null) {
            String profileImageUrl = s3Service.upload(changeUserInfoRequestDto.getFile(), "profile");
            String profileImageUrlConvert = profileImageUrl.substring(8);
            changeUserInfoResponseDto.setProfileImageUrl("http://" + profileImageUrlConvert);
        }
        changeUserInfoResponseDto.setUpdateUserNickname(changeUserInfoRequestDto.getUpdateUserNickname());
        changeUserInfoResponseDto.setMajor1(changeUserInfoRequestDto.getMajor1());
        changeUserInfoResponseDto.setMajor2(changeUserInfoRequestDto.getMajor2());
        changeUserInfoResponseDto.setIntroduce("".equals(changeUserInfoRequestDto.getIntroduce()) ? null : changeUserInfoRequestDto.getIntroduce());
        user.changeUserInfo(changeUserInfoResponseDto);
        userRepository.save(user);
    }

    @Transactional
    public void addIntroduce(AddIntroduceRequestDto addIntroduceRequestDto) {
        User user = userRepository.findByUserEmailAndLeaveYn(addIntroduceRequestDto.getUserEmail(), false).orElseThrow(
                () -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        user.setIntroduce(addIntroduceRequestDto.getIntroduce());
        userRepository.save(user);
    }

    @Transactional
    public String giveAccessToken(RefreshTokenRequestDto refreshTokenRequestDto) {
        User user = userRepository.findByUserEmailAndLeaveYn(refreshTokenRequestDto.getUserEmail(), false).orElseThrow(
                () -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user).orElseThrow(
                () -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        if (jwtUtil.validateRefreshToken(refreshToken.getRefreshToken(), refreshTokenRequestDto.getUserEmail())) {
            return jwtUtil.generateToken(user);
        }
        return null;
    }

    @Transactional
    public BothTokenResponseDto giveBothToken(RefreshTokenRequestDto refreshTokenRequestDto) {
        User user = userRepository.findByUserEmailAndLeaveYn(refreshTokenRequestDto.getUserEmail(), false).orElseThrow(
                () -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        RefreshToken refreshTokenInfo = refreshTokenRepository.findByUser(user).orElseThrow(
                () -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(refreshTokenRequestDto.getUserEmail());
        refreshTokenInfo.setRefreshToken(user, refreshToken);
        UUID refreshTokenSeq = refreshTokenRepository.save(refreshTokenInfo).getRefreshTokenSeq();
        log.info(">>>>> : {}", refreshTokenInfo.getRefreshTokenSeq());
        return new BothTokenResponseDto(accessToken, refreshTokenSeq);
    }

    @Transactional(readOnly = true)
    public FindUserInfoResponseDto getUserInfo(Long userSeq) {
        User user = userRepository.findByUserSeqAndLeaveYn(userSeq, false).orElseThrow(
                () -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        return new FindUserInfoResponseDto(user.getUserSeq(), user.getUserNickname(), user.getMajor1(), user.getMajor2(), user.getIntroduce(), user.getFollower(), user.getFollowing(), user.getProfileImageUrl());
    }

    @Transactional
    @CacheEvict(value = "feed", allEntries = true)
    public TokenResponseDto loginFlow(LoginRequestDto loginRequestDto) throws Exception {

        //nickname 또는 major 1 또는 major 2 가 비어있으면 회원가입
        if ("".equals(loginRequestDto.getUserNickname()) && "".equals(loginRequestDto.getMajor1()) && "".equals(loginRequestDto.getMajor2())
                && !userRepository.existsByUserEmailAndLeaveYn(loginRequestDto.getUserEmail(), false)) {
            return new TokenResponseDto("발급x", UUID.randomUUID(), LoginStatus.회원가입, 0L, "", "", "", 0, 0);
        }
        //기존 회원이 있다면
        if (userRepository.existsByUserEmailAndLeaveYn(loginRequestDto.getUserEmail(), false)) {
            User userInfo = userRepository.findByUserEmailAndLeaveYn(loginRequestDto.getUserEmail(), false).orElseThrow(() -> new UsernameNotFoundException("User not found with userId: " + loginRequestDto.getUserEmail()));
            RefreshToken refreshTokenInfo = refreshTokenRepository.findByUser(userInfo).orElseThrow(() ->
                    new SummarCommonException(SummarErrorCode.WRONG_TOKEN.getCode(), SummarErrorCode.WRONG_TOKEN.getMessage()));


            //access token 생성
            final String accessToken = jwtUtil.generateToken(userInfo);
            //refresh token 생성
            final String refreshToken = jwtUtil.generateRefreshToken(loginRequestDto.getUserEmail());
            refreshTokenInfo.setRefreshToken(userInfo, refreshToken);
            UUID refreshTokenSeq = refreshTokenRepository.save(refreshTokenInfo).getRefreshTokenSeq();
            log.info(">>>>> : {}", refreshTokenSeq);

            //로그인 이력 업데이트
            userInfo.setLastLoginDateAndDeviceToken(LocalDate.now(), loginRequestDto.getDeviceToken());

            userRepository.save(userInfo);


            return new TokenResponseDto(accessToken, refreshTokenSeq, LoginStatus.로그인, userInfo.getUserSeq(),
                    userInfo.getUserNickname() == null ? "" : userInfo.getUserNickname(),
                    userInfo.getMajor1() == null ? "" : userInfo.getMajor1(),
                    userInfo.getMajor2() == null ? "" : userInfo.getMajor2(),
                    userInfo.getFollower() == null ? 0 : userInfo.getFollower(),
                    userInfo.getFollowing() == null ? 0 : userInfo.getFollowing());
        }
        //신규 회원이라면
        userRepository.save(new User(new UserSaveDto(loginRequestDto.getUserEmail(), loginRequestDto.getUserNickname(), loginRequestDto.getMajor1(), loginRequestDto.getMajor2(), 0, 0, loginRequestDto.getSocialType(), LocalDate.now(), loginRequestDto.getDeviceToken(), true, false)));
        User user = userRepository.findByUserEmailAndLeaveYn(loginRequestDto.getUserEmail(), false).orElseThrow(() ->
                new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        RefreshToken refreshTokenInfo = new RefreshToken();

        //access token 생성
        final String accessToken = jwtUtil.generateToken(user);
        //refresh token 생성
        final String refreshToken = jwtUtil.generateRefreshToken(loginRequestDto.getUserEmail());
        refreshTokenInfo.setRefreshToken(user, refreshToken);

        UUID refreshTokenSeq = refreshTokenRepository.save(refreshTokenInfo).getRefreshTokenSeq();
        log.info(">>>>> : {}", refreshTokenSeq);
        return new TokenResponseDto(accessToken, refreshTokenSeq, LoginStatus.회원가입완료, user.getUserSeq(), "", "", "", 0, 0);
    }

    @Transactional(readOnly = true)
    public SearchUserInfoResponseDto searchUserInfo(Long userSeq) {
        User user = userRepository.findByUserSeqAndLeaveYn(userSeq, false).orElseThrow(() ->
                new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        return new SearchUserInfoResponseDto(user.getUserSeq(), user.getUserEmail(), user.getFollower(), user.getFollowing(), user.getUserNickname(), user.getMajor1(), user.getMajor2());
    }

    @Transactional(readOnly = true)
    public Page<SearchUserListResponseDto> searchUserList(String userNickname, Pageable pageable) {
        //닉네임 검색 완성했을 때
        boolean searchUserListCheck = userRepository.existsByUserNicknameContainsAndLeaveYn(userNickname, false);
        if (searchUserListCheck) {
            Page<User> searchUserList = userRepository.findByUserNicknameContainsAndLeaveYn(userNickname, false, pageable);
            return searchUserList.map(SearchUserListResponseDto::new);
        }
        List<String> index_list = new ArrayList<>();
        index_list.add("ㄱ");
        index_list.add("ㄴ");
        index_list.add("ㄷ");
        index_list.add("ㄹ");
        index_list.add("ㅁ");
        index_list.add("ㅂ");
        index_list.add("ㅅ");
        index_list.add("ㅇ");
        index_list.add("ㅈ");
        index_list.add("ㅊ");
        index_list.add("ㅋ");
        index_list.add("ㅌ");
        index_list.add("ㅍ");
        index_list.add("ㅎ");
        Map<Integer, String> index_map = new HashMap<>();
        index_map.put(0, "가");
        index_map.put(1, "나");
        index_map.put(2, "다");
        index_map.put(3, "라");
        index_map.put(4, "마");
        index_map.put(5, "바");
        index_map.put(6, "사");
        index_map.put(7, "아");
        index_map.put(8, "자");
        index_map.put(9, "차");
        index_map.put(10, "카");
        index_map.put(11, "타");
        index_map.put(12, "파");
        index_map.put(13, "하");
        index_map.put(14, "힣");
        //닉네임 검색 초성일 때
        int num = 0;
        for (int i = 0; i < index_list.size(); i++) {

            if (userNickname.equals(index_list.get(i))) {
                num = i;
                break;
            }
        }
        Page<User> users = userRepository.searchWord(index_map.get(num), index_map.get(num + 1), pageable);
        return users.map(SearchUserListResponseDto::new);
    }

    @Transactional
    public void changePushNotification(PushNotificationStatusDto pushNotificationStatusDto) {
        User user = userRepository.findByUserNicknameAndLeaveYn(pushNotificationStatusDto.getUserNickname(), false)
                .orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        user.setPushAlarmYn(pushNotificationStatusDto.getStatus());
        userRepository.save(user);
    }

    public UserPushStatusInfoResponseDto userPushStatusInfo(Long userSeq) {
        User user = userRepository.findByUserSeqAndLeaveYn(userSeq, false)
                .orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        UserPushStatusInfoResponseDto userPushStatusInfoResponseDto = new UserPushStatusInfoResponseDto();
        userPushStatusInfoResponseDto.setStatus(user.getPushAlarmYn());
        return userPushStatusInfoResponseDto;
    }

    @Transactional
    public void leaveUser(Long userSeq) {
        User userInfo = userRepository.findByUserSeqAndLeaveYn(userSeq, false)
                .orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        List<Follow> followingList = followRepository.findByFollowingUserAndFollowYn(userInfo, true);
        //팔로윙 한 사람 초기화
        if (!ObjectUtils.isEmpty(followingList)) {
            for (Follow followingInfo : followingList) {
                followingInfo.setFollowYn(false);
                followRepository.save(followingInfo);
                User followingUser = followingInfo.getFollowingUser();
                User followedUser = followingInfo.getFollowedUser();
                Integer value1 = followRepository.countByFollowingUserAndFollowYn(followingUser, true);
                Integer value2 = followRepository.countByFollowedUserAndFollowYn(followedUser, true);
                followingUser.updateFollowing(value1);
                followedUser.updateFollower(value2);
                userRepository.save(followingUser);
                userRepository.save(followedUser);
            }
        }
        //팔로우 당한 사람 초기화
        List<Follow> followedList = followRepository.findByFollowedUserAndFollowYn(userInfo, true);
        if (!ObjectUtils.isEmpty(followedList)) {
            for (Follow followerInfo : followedList) {
                followerInfo.setFollowYn(false);
                followRepository.save(followerInfo);
                User followingUser = followerInfo.getFollowingUser();
                User followedUser = followerInfo.getFollowedUser();
                Integer value1 = followRepository.countByFollowingUserAndFollowYn(followingUser, true);
                Integer value2 = followRepository.countByFollowedUserAndFollowYn(followedUser, true);
                followingUser.updateFollowing(value1);
                followedUser.updateFollower(value2);
                userRepository.save(followingUser);
                userRepository.save(followedUser);
            }
        }
        //leaveYn = 'Y' 와 유저 정보 초기화
        userInfo.leaveUser("", "", "", false, true);

        userRepository.save(userInfo);
    }

    @Transactional
    public boolean blockUser(Long blockedUserSeq) {
        Optional<User> user = userRepository.findById(jwtUtil.getCurrentUserSeq());
        if (user.isPresent()) {
            Optional<User> blockedUser = userRepository.findById(blockedUserSeq);
            blockedUser.ifPresent(block -> {
                userBlockRepository.findByUserUserSeqAndBlockedUserUserSeq(user.get().getUserSeq(), block.getUserSeq())
                        .ifPresentOrElse(
                                userBlockRepository::delete
                                ,
                                () -> userBlockRepository.save(new UserBlock(user.get(), block))
                        );
            });
            return true;
        } else {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<UserBlockDTO> getBlockedUserList() {
        List<UserBlockDTO> userBlockDTOList = new ArrayList<>();
        Optional<User> user = userRepository.findById(jwtUtil.getCurrentUserSeq());
        if (user.isPresent()) {
           Optional<List<UserBlock>> userBlockList = userBlockRepository.findByUserUserSeq(user.get().getUserSeq());
            userBlockList.ifPresent(userBlocks -> userBlocks.forEach(
                    userBlock -> userBlockDTOList.add(new UserBlockDTO(userBlock.getBlockedUser()))
            ));
        }
        return userBlockDTOList;
    }
}
