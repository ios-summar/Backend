package com.summar.summar.service;

import com.summar.summar.common.SummarCommonException;
import com.summar.summar.common.SummarErrorCode;
import com.summar.summar.domain.Follow;
import com.summar.summar.domain.GatheringNotification;
import com.summar.summar.domain.User;
import com.summar.summar.dto.*;
import com.summar.summar.enumeration.FollowStatus;
import com.summar.summar.enumeration.NotificationType;
import com.summar.summar.repository.FollowRepository;
import com.summar.summar.repository.GatheringNotificationRepository;
import com.summar.summar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final PushService pushService;
    private final GatheringNotificationRepository gatheringNotificationRepository;


    @Transactional
    public Page<FollowerResponseDto> findFollowers(Long userSeq, Pageable pageable) {
        //해당 유저의 팔로워들 정보 추출
        //나를 팔로우하는사람들 리스트
        //userSeq = 1
        User userInfo = userRepository.findByUserSeqAndLeaveYn(userSeq, false).orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        Page<Follow> followingList = followRepository.findByFollowedUserAndFollowYn(userInfo, true, pageable);

        return followingList.map(m -> FollowerResponseDto.builder()
                .userNickname(m.getFollowingUser().getUserNickname())
                .follower(m.getFollowingUser().getFollower())
                .following(m.getFollowingUser().getFollowing())
                .major1(m.getFollowingUser().getMajor1())
                .major2(m.getFollowingUser().getMajor2())
                .profileImageUrl(m.getFollowingUser().getProfileImageUrl())
                .userSeq(m.getFollowingUser().getUserSeq())
                .followUp(m.getFollowUp())
                .build());
    }

    @Transactional(readOnly = true)
    public Page<?> findFollowings(Long userSeq, Pageable pageable) {
        //내가 팔로우 하는 사람들 리스트
        User userInfo = userRepository.findByUserSeqAndLeaveYn(userSeq, false).orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        Page<Follow> followingList = followRepository.findByFollowingUserAndFollowYn(userInfo, true, pageable);
        return followingList.map(m -> FollowerResponseDto.builder()
                .userNickname(m.getFollowedUser().getUserNickname())
                .follower(m.getFollowedUser().getFollower())
                .following(m.getFollowedUser().getFollowing())
                .major1(m.getFollowedUser().getMajor1())
                .major2(m.getFollowedUser().getMajor2())
                .profileImageUrl(m.getFollowedUser().getProfileImageUrl())
                .userSeq(m.getFollowedUser().getUserSeq())
                .followUp(m.getFollowUp())
                .build());
    }

    @Transactional
    public void addFollower(FollowerRequestDto followerRequestDto) {
        if (followerRequestDto.getFollowedUserSeq().equals(followerRequestDto.getFollowingUserSeq())) {
            throw new NotFoundException("같을수없다");
        }
        User followedUser = userRepository.findByUserSeqAndLeaveYn(followerRequestDto.getFollowedUserSeq(), false).orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        User followingUser = userRepository.findByUserSeqAndLeaveYn(followerRequestDto.getFollowingUserSeq(), false).orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        Follow followInfo1 = followRepository.findByFollowedUserAndFollowingUserAndFollowYn(followedUser, followingUser, false).orElse(null);


        //푸쉬 알람
        PushNotificationDto pushNotificationDto = PushNotificationDto.builder()
                .title("Summar")
                .body(followingUser.getUserNickname() + "님이 회원님을 팔로우했어요.")
                .userNickname(followedUser.getUserNickname())
                .userSeq(followingUser.getUserSeq())
                .pushType("팔로우")
                .build();
        //팔로우 정보가 없다면
        if (followInfo1 == null) {
            FollowerSaveDto followerSaveDto = FollowerSaveDto.builder()
                    .followedUser(followedUser)
                    .followYn(true)
                    .followingUser(followingUser)
                    .build();
            followRepository.save(new Follow(followerSaveDto));
            Integer followerCount = followRepository.countByFollowedUserAndFollowYn(followedUser, true);
            Integer followingCount = followRepository.countByFollowingUserAndFollowYn(followingUser, true);
            followedUser.updateFollower(followerCount);
            userRepository.save(followedUser);
            followingUser.updateFollowing(followingCount);
            userRepository.save(followingUser);

            //맞팔을 위한 비교
            Follow follow1 = followRepository.findByFollowedUserAndFollowingUserAndFollowYn(followedUser, followingUser, true).orElse(null);
            Follow follow2 = followRepository.findByFollowedUserAndFollowingUserAndFollowYn(followingUser, followedUser, true).orElse(null);
            if (follow1 != null && follow2 != null) {
                if (follow1.getFollowedUser().equals(follow2.getFollowingUser()) && follow1.getFollowingUser().equals(follow2.getFollowedUser())) {
                    follow1.setFollowUp(true);
                    follow2.setFollowUp(true);
                    followRepository.save(follow1);
                    followRepository.save(follow2);
                }
            }
            //알림리스트에 저장
            GatheringNotification gatheringNotification = new GatheringNotification(
                    GatheringNotificationSaveDto.builder()
                            .userSeq(followedUser)
                            .otherUserSeq(followingUser)
                            .notificationType(NotificationType.팔로우)
                            .delYn(false)
                            .build());
            gatheringNotificationRepository.save(gatheringNotification);
        } else {
            //기존 팔로우 정보가 있다면
            followInfo1.setFollowYn(true);
            followRepository.save(followInfo1);
            Integer followerCount = followRepository.countByFollowedUserAndFollowYn(followedUser, true);
            Integer followingCount = followRepository.countByFollowingUserAndFollowYn(followingUser, true);
            followedUser.updateFollower(followerCount);
            userRepository.save(followedUser);
            followingUser.updateFollowing(followingCount);
            userRepository.save(followingUser);

            //맞팔을 위한 비교
            Follow follow1 = followRepository.findByFollowedUserAndFollowingUserAndFollowYn(followedUser, followingUser, true).orElse(null);
            Follow follow2 = followRepository.findByFollowedUserAndFollowingUserAndFollowYn(followingUser, followedUser, true).orElse(null);
            if (follow1 != null && follow2 != null) {
                if (follow1.getFollowedUser().equals(follow2.getFollowingUser()) && follow1.getFollowingUser().equals(follow2.getFollowedUser())) {
                    follow1.setFollowUp(true);
                    follow2.setFollowUp(true);
                    followRepository.save(follow1);
                    followRepository.save(follow2);
                }
            }
            //기존에 있던 알림 정보 업데이트 [list 일리는 없지만 혹시나를 위해 list 타입 지정]
            List<GatheringNotification> gatheringNotification = gatheringNotificationRepository.findAllByNotificationTypeAndUserSeqAndOtherUserSeq(NotificationType.팔로우,followedUser,followingUser);
            if(!ObjectUtils.isEmpty(gatheringNotification)){
                gatheringNotification.get(0).setCreatedDate(LocalDateTime.now());
                gatheringNotificationRepository.save(gatheringNotification.get(0));
            }
        }
        //푸시알림 발송
        pushService.pushNotification(pushNotificationDto);
    }

    @Transactional
    public void deleteFollower(FollowerRequestDto followerRequestDto) {
        User followingUser = userRepository.findByUserSeqAndLeaveYn(followerRequestDto.getFollowingUserSeq(), false).orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        User followedUser = userRepository.findByUserSeqAndLeaveYn(followerRequestDto.getFollowedUserSeq(), false).orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        Follow followInfo1 = followRepository.findByFollowedUserAndFollowingUserAndFollowYn(followedUser, followingUser, true).orElse(null);
        Follow followInfo2 = followRepository.findByFollowedUserAndFollowingUserAndFollowYn(followingUser, followedUser, true).orElse(null);
        if (followInfo1 != null && followInfo2 != null) {
            followInfo1.setFollowUp(false);
            followInfo2.setFollowUp(false);
            followRepository.save(followInfo1);
            followRepository.save(followInfo2);
        }
        followInfo1.setFollowYn(false);
        Integer followerCount = followRepository.countByFollowedUserAndFollowYn(followedUser, true);
        Integer followingCount = followRepository.countByFollowingUserAndFollowYn(followingUser, true);
        followedUser.updateFollower(followerCount);
        followingUser.updateFollowing(followingCount);
        userRepository.save(followingUser);
        userRepository.save(followedUser);
    }

    @Transactional(readOnly = true)
    //followedUserSeq = 팔로우 당한사람
    //followingUserSeq = 팔로우 한사람
    public Boolean followCheck(Long followedUserSeq, Long followingUserSeq) {
        User followedUser = userRepository.findByUserSeqAndLeaveYn(followedUserSeq, false)
                .orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        User followingUser = userRepository.findByUserSeqAndLeaveYn(followingUserSeq, false)
                .orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        return followRepository.existsByFollowedUserAndFollowingUserAndFollowYn(followedUser, followingUser, true);
    }

    @Transactional(readOnly = true)
    public Page<FollowerResponseDto> searchOtherFollowers(OtherFollowersCheckRequestDto otherFollowerCheckRequestDto, Pageable pageable) {
        //3
        User otherUser = userRepository.findById(otherFollowerCheckRequestDto.getOtherSeq())
                .orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        //10
        User user = userRepository.findById(otherFollowerCheckRequestDto.getUserSeq())
                .orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        //다른사람을 팔로우하고있는 사람들 리스트
        Page<Follow> otherFollowerList = followRepository.findByFollowedUserAndFollowYn(otherUser, true, pageable);
        int index = 0;
        for (Follow followCheck : otherFollowerList) {
            //나자신인경우
            if(user.equals(followCheck.getFollowingUser())){
                otherFollowerList.getContent().get(index).setFollowStatus(FollowStatus.나자신);  // "나자신"
                index++;
                continue;
            }
            // 맞팔인 경우
            if (followRepository.existsByFollowedUserAndFollowingUserAndFollowYn(user, followCheck.getFollowingUser(), true) &&
                    followRepository.existsByFollowedUserAndFollowingUserAndFollowYn(followCheck.getFollowingUser(), user, true)) {
                otherFollowerList.getContent().get(index).setFollowStatus(FollowStatus.맞팔);  // "맞팔"
                index++;
                continue;
            }
            //userSeq -> otherSeq 팔로우 인 경우.
            if (followRepository.existsByFollowedUserAndFollowingUserAndFollowYn(followCheck.getFollowingUser(), user, true)) {
                otherFollowerList.getContent().get(index).setFollowStatus(FollowStatus.한쪽팔로우); // "한쪽팔로우";
                index++;
                continue;
            }
            //둘다 팔로우 아님
            otherFollowerList.getContent().get(index).setFollowStatus(FollowStatus.암것도아님);
            index++;
            continue;
/*            //둘다 팔로우 아닌 경우
            if (!followRepository.existsByFollowedUserAndFollowingUserAndFollowYn(user, followCheck.getFollowingUser(), true) &&
                    !followRepository.existsByFollowedUserAndFollowingUserAndFollowYn(followCheck.getFollowingUser(), user, true)) {
                otherFollowerList.getContent().get(index).setFollowStatus(FollowStatus.둘다팔로우아님);  // "둘다팔로우아님";
                index++;
                continue;
            }*/
        }
        return otherFollowerList.map(m -> FollowerResponseDto.builder()
                .userNickname(m.getFollowingUser().getUserNickname())
                .follower(m.getFollowingUser().getFollower())
                .following(m.getFollowingUser().getFollowing())
                .major1(m.getFollowingUser().getMajor1())
                .major2(m.getFollowingUser().getMajor2())
                .profileImageUrl(m.getFollowingUser().getProfileImageUrl())
                .userSeq(m.getFollowingUser().getUserSeq())
                .followUp(false)
                .followStatus(m.getFollowStatus())
                .build());
    }

    public Page<FollowerResponseDto> searchOtherFollowings(OtherFollowersCheckRequestDto otherFollowerCheckRequestDto, Pageable pageable) {
        //6
        User otherUser = userRepository.findById(otherFollowerCheckRequestDto.getOtherSeq())
                .orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        //10
        User user = userRepository.findById(otherFollowerCheckRequestDto.getUserSeq())
                .orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        //다른사람을 팔로잉하고있는 사람들 리스트
        Page<Follow> otherFollowerList = followRepository.findByFollowingUserAndFollowYn(otherUser, true, pageable);
        int index = 0;
        for (Follow followCheck : otherFollowerList) {
            //나 자신
            if(user.getUserSeq().equals(followCheck.getFollowedUser().getUserSeq())){
                otherFollowerList.getContent().get(index).setFollowStatus(FollowStatus.나자신);  // "나자신"
                index++;
                continue;
            }
            //맞팔인 경우
            else if (followRepository.existsByFollowedUserAndFollowingUserAndFollowYn(user, followCheck.getFollowedUser(), true) &&
                    followRepository.existsByFollowedUserAndFollowingUserAndFollowYn(followCheck.getFollowedUser(), user, true)) {
                otherFollowerList.getContent().get(index).setFollowStatus(FollowStatus.맞팔);  // "맞팔"
                index++;
                continue;
            }
            //누구 한쪽이 팔로우 인경우
            else if (followRepository.existsByFollowedUserAndFollowingUserAndFollowYn(followCheck.getFollowedUser(), user, true)) {
                otherFollowerList.getContent().get(index).setFollowStatus(FollowStatus.한쪽팔로우); // "한쪽팔로우";
                index++;
                continue;
                //아무것도 아닐때
            }else{
                otherFollowerList.getContent().get(index).setFollowStatus(FollowStatus.암것도아님);
                index++;
                continue;
            }

            // "암것도아님";
  /*          //둘다 팔로우 아닌 경우
            if (!followRepository.existsByFollowedUserAndFollowingUserAndFollowYn(user, followCheck.getFollowedUser(), true) &&
                    !followRepository.existsByFollowedUserAndFollowingUserAndFollowYn(followCheck.getFollowedUser(), user, true)) {
                otherFollowerList.getContent().get(index).setFollowStatus(FollowStatus.둘다팔로우아님);  // "둘다팔로우아님";
                index++;
                continue;
            }*/
        }
        return otherFollowerList.map(m -> FollowerResponseDto.builder()
                .userNickname(m.getFollowedUser().getUserNickname())
                .follower(m.getFollowedUser().getFollower())
                .following(m.getFollowedUser().getFollowing())
                .major1(m.getFollowedUser().getMajor1())
                .major2(m.getFollowedUser().getMajor2())
                .profileImageUrl(m.getFollowedUser().getProfileImageUrl())
                .userSeq(m.getFollowedUser().getUserSeq())
                .followUp(false)
                .followStatus(m.getFollowStatus())
                .build());
    }
}
