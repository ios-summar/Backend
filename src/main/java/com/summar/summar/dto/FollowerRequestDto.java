package com.summar.summar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FollowerRequestDto {
    @Schema(description = "팔로우신청받은사람", example = "팔로우신청받은사람")
    private String followedUserNickname; //팔로우신청받은사람
    @Schema(description = "팔로우 건사람", example = "팔로우 건사람")
    private String followingUserNickname; //팔로우 건사람
}
