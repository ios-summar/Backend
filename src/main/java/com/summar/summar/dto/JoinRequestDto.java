package com.summar.summar.dto;


import com.summar.summar.enumeration.SocialType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;


/**
 * A DTO for the {@link com.summar.summar.domain.User} entity.
 */

@Getter
@Setter
public class JoinRequestDto {


    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String userId;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String userPwd;

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String userName;

    @NotBlank(message = "필명은 필수 입력 값입니다.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9-_]{2,10}$", message = "닉네임은 특수문자를 제외한 2~10자리여야 합니다.")
    private String userNickname;

    @NotBlank(message = "휴대폰번호는 필수 입력 값입니다.")
    //@Pattern(regexp = "/^01([0|1|6|7|8|9]?)-?([0-9]{3,4})-?([0-9]{4})$/", message = "휴대폰 번호를 제대로 입력해주세요.")

    private String userHpNo;

    private SocialType socialType;

}
