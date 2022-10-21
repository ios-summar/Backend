package com.summar.summar.domain;

import com.summar.summar.dto.JoinRequestDto;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@Table(name = "USER")
@Entity
public class User extends BaseTimeEntity implements Serializable {

    /**
     * This VO is for security.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userSeq;

    private String userId;

    private String userPwd;

    private String userName;

    private String userNickname;

    private String userHpNo;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private RefreshToken refreshToken;

}
