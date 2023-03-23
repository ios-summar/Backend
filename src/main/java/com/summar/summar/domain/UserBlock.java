package com.summar.summar.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.summar.summar.dto.UserBlockDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;


@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UserBlock extends BaseTimeEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userBlockSeq;

    @ManyToOne
    @JoinColumn(name="user_seq", referencedColumnName = "user_seq")
    @JsonIgnoreProperties(value = { "userList" }, allowSetters = true)
    private User user; //차단한 사용자

    @ManyToOne
    @JoinColumn(name="blocked_user_seq", referencedColumnName = "user_seq")
    @JsonIgnoreProperties(value = { "blockedUserList" }, allowSetters = true)
    private User blockedUser; //차단할 사용자

    public UserBlock(User user, User blockedUser){
        this.user = user;
        this.blockedUser = blockedUser;
    }

}
