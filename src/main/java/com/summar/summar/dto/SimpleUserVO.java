package com.summar.summar.dto;


import com.summar.summar.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class SimpleUserVO implements Serializable {
    @Schema(description = "유저 시퀀스")
    private Long userSeq;
    @Schema(description = "유저 이메일")
    private String userEmail;
    @Schema(description = "전공 계열")
    private String major1; //계열
    @Schema(description = "세부 전공")
    private String major2; //전공
    @Schema(description = "닉네임")
    private String userNickname; //닉네임
    @Schema(description = "프로필 이미지 경로")
    private String profileImageUrl; //프로필이미지경로
    @Schema(description = "탈퇴 여부")
    private boolean leaveYn;
    @Schema(description = "팔로워 수")
    private int follower;
    @Schema(description = "팔로잉 수")
    private int following;
    @Schema(description = "내가 차단한 사용자 인지 여부")
    private boolean blocked;

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public SimpleUserVO(User user){
        this.userSeq = user.getUserSeq();
        this.userEmail = user.getUserEmail();
        this.major1 = user.getMajor1();
        this.major2 = user.getMajor2();
        this.userNickname = user.getUserNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.leaveYn = user.getLeaveYn();
        this.follower=user.getFollower();
        this.following=user.getFollowing();
    }

    public SimpleUserVO(User user, boolean blocked){
        this.userSeq = user.getUserSeq();
        this.userEmail = user.getUserEmail();
        this.major1 = user.getMajor1();
        this.major2 = user.getMajor2();
        this.userNickname = user.getUserNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.leaveYn = user.getLeaveYn();
        this.follower=user.getFollower();
        this.following=user.getFollowing();
        this.blocked = blocked;
    }
}
