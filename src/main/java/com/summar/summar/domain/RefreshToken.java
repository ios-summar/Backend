package com.summar.summar.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "REFRESH_TOKEN")
public class RefreshToken extends BaseTimeEntity implements Serializable {
    @Id @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2",strategy = "uuid2")
    @Type(type = "uuid-char")
    private UUID refreshTokenSeq;
    private String refreshToken;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_seq")
    private User user;


    public RefreshToken() {
        this.refreshTokenSeq = UUID.randomUUID();
    }

    public void setRefreshToken(User user, String refreshToken) {
        this.user = user;
        this.refreshToken = refreshToken;
    }
}
