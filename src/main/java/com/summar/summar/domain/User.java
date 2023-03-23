package com.summar.summar.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.summar.summar.dto.*;
import com.summar.summar.enumeration.SocialType;
import com.summar.summar.enumeration.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "users")
@Entity
@Table(name = "USER")
public class User extends BaseTimeEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_seq")
    private Long userSeq;
    @Field(type = FieldType.Keyword)
    private String userNickname; //닉네임
    private String userEmail; //이메일

    private LocalDate lastLoginDate; //최종로그인일시
    @Enumerated(EnumType.STRING)
    private SocialType socialType; //소셜로그인 타입
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus = UserStatus.normal; //유저 상태
    private String major1; //계열
    private String major2; //전공

    private String introduce; //자기소개

    private Integer follower; //팔로워

    private Integer following; //팔로윙

    private String profileImageUrl; //프로필이미지경로

    private String deviceToken; //디바이스 식별 토큰값 (랜덤)

    @Type(type="yes_no")
    private Boolean leaveYn; //탈퇴 여부

    @OneToMany(mappedBy = "followingUser", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Follow> followingUserList;

    @OneToMany(mappedBy = "followedUser", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Follow> followedUserList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<FeedLike> feedLikes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<FeedScrap> feedScraps = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"user"}, allowSetters = true)
    private List<FeedComment> feedComments = new ArrayList<>();
    @Type(type="yes_no")
    private Boolean pushAlarmYn;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference
    private RefreshToken refreshToken;

    @OneToMany(mappedBy = "userSeq",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<GatheringNotification> gatheringNotificationsOfUserList;

    @OneToMany(mappedBy = "otherUserSeq",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<GatheringNotification> gatheringNotificationsOfOtherUserList;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<Report> reportsOfUsers = new ArrayList<>();

    @OneToMany(mappedBy = "otherUser",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<Report> reportsOfOtherUsers = new ArrayList<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<UserBlock> userList = new ArrayList<>();

    @OneToMany(mappedBy = "blockedUser", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<UserBlock> blockedUserList = new ArrayList<>();

    public void setFollower(Integer follower) {
        this.follower = follower;
    }

    public void setFollowing(Integer following) {
        this.following = following;
    }

    @Builder
    public User(Long userSeq, String userEmail, String userNickname) {
        this.userSeq = userSeq;
        this.userEmail = userEmail;
        this.userNickname = userNickname;
    }


    public User(LoginRequestDto loginRequestDto) {
        this.userNickname = loginRequestDto.getUserNickname();
        this.userEmail = loginRequestDto.getUserEmail();
    }

    public User(UserSaveDto userSaveDto){
        this.userEmail = userSaveDto.getUserEmail();
        this.userNickname = userSaveDto.getUserNickname();
        this.major1 = userSaveDto.getMajor1();
        this.major2 = userSaveDto.getMajor2();
        this.follower = userSaveDto.getFollower();
        this.following = userSaveDto.getFollowing();
        this.deviceToken = userSaveDto.getDeviceToken();
        this.socialType = userSaveDto.getSocialType();
        this.lastLoginDate = userSaveDto.getLastLoginDate();
        this.pushAlarmYn = userSaveDto.getPushAlarmYn();
        this.leaveYn = userSaveDto.getLeaveYn();
    }
    public void setLastLoginDateAndDeviceToken(LocalDate lastLoginDate,String deviceToken){
        this.lastLoginDate = lastLoginDate;
        this.deviceToken = deviceToken;
    }
    public void setUserStatus(UserStatus userStatus){
        this.userStatus = userStatus;
    }

    public void setIntroduce(String introduce){
        this.introduce = introduce;
    }

    public void setPushAlarmYn(Boolean pushStatus){
        this.pushAlarmYn = pushStatus;
    }

    public void changeUserInfo(ChangeUserInfoResponseDto changeUserInfoResponseDto) {
        this.userNickname = changeUserInfoResponseDto.getUpdateUserNickname();
        this.major1 = changeUserInfoResponseDto.getMajor1();
        this.major2 = changeUserInfoResponseDto.getMajor2();
        this.profileImageUrl = changeUserInfoResponseDto.getProfileImageUrl();
        this.introduce = changeUserInfoResponseDto.getIntroduce();
    }
    public void updateFollower(Integer follower){
        this.follower = follower;
    }

    public void updateFollowing(Integer following) {
        this.following = following;
    }

    public void leaveUser(String userEmail, String userNickname, String deviceToken,Boolean pushAlarmYn,Boolean leaveYn){
        this.userEmail = userEmail;
        this.userNickname = userNickname;
        this.deviceToken = deviceToken;
        this.pushAlarmYn = pushAlarmYn;
        this.leaveYn = leaveYn;
    }
}
