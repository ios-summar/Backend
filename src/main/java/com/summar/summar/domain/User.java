package com.summar.summar.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.summar.summar.dto.LoginRequestDto;
import com.summar.summar.enumeration.SocialType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "USER")
public class User extends BaseTimeEntity implements Serializable {

    /**
     * This VO is for security.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userSeq;
    private String userNickname;
    private String userEmail;

    private LocalDate lastLoginDate;


    @Enumerated(EnumType.STRING)
    private SocialType socialType = SocialType.APPLE;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus = UserStatus.normal;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private RefreshToken refreshToken;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="major_seq")
    private Major major;

    @Builder
    public User(Long userSeq, String userEmail, String userNickname) {
        this.userSeq = userSeq;
        this.userEmail = userEmail;
        this.userNickname = userNickname;
    }

    public User(LoginRequestDto loginRequestDto) {
        this.userNickname = loginRequestDto.getUserNickName();
        this.userEmail = loginRequestDto.getUserEmail();
        this.major = loginRequestDto.getMajor();
    }

}
