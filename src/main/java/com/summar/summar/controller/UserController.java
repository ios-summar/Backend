package com.summar.summar.controller;

import com.summar.summar.dto.*;
import com.summar.summar.results.*;
import com.summar.summar.service.UserService;
import com.summar.summar.util.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;

@RestController
@Slf4j
@RequiredArgsConstructor
@Api(tags = {"유저 관련 API 제공 controller"})
@RequestMapping(value = "/api/v1/user")
public class UserController {
    private final UserService userService;



    /**
     * 로그인 & 회원가입
     * @param loginRequestDto
     * @return
     * @throws Exception
     */

    @Operation(summary = "회원가입 & 로그인", description = "토큰이 발급 됩니다,")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리", content = @Content(examples = @ExampleObject(value = StringUtil.login))),
            @ApiResponse(responseCode = "403", description = "권한 없음(다른 회원의 계정 변경)", content = @Content(examples = @ExampleObject(value = StringUtil.nulls))),
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) throws Exception {
        return AuthenticationResult.build(userService.loginFlow(loginRequestDto));

    }
    /**
     * 회원 탈퇴
     * @param userSeq
     * @return
     */
    @Operation(summary = "회원탈퇴", description = "회원이 탈퇴됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리", content = @Content(examples = @ExampleObject(value = StringUtil.leaveUser))),
            @ApiResponse(responseCode = "403", description = "권한 없음(다른 회원의 계정 변경)", content = @Content(examples = @ExampleObject(value = StringUtil.nulls))),
    })
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/leave")
    public ResponseEntity<?> leave(@RequestParam(value = "userSeq")Long userSeq){
        userService.leaveUser(userSeq);
        return ObjectResult.ok();
    }


    /**
     * 리프레시 토큰으로 엑세스 토큰 재발급
     *
     * @param refreshTokenRequestDto
     * @return
     */
    @Operation(summary = "리프레시토큰이 유효할 시 액새스토큰 재발급", description = "토큰이 재발급 됩니다,")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리", content = @Content(examples = @ExampleObject(value = StringUtil.giveAccessToken))),
            @ApiResponse(responseCode = "403", description = "권한 없음(다른 회원의 계정 변경)", content = @Content(examples = @ExampleObject(value = StringUtil.nulls))),
    })
    @PostMapping("/give-access-token")
    public ResponseEntity<?> giveAccessToken(@RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        return ObjectResult.build("accessToken",  userService.giveAccessToken(refreshTokenRequestDto));
    }
    /**
     * 액세스 토큰, 리프레시 토큰 재발급
     * @param refreshTokenRequestDto
     * @return
     */
    @Operation(summary = "리프래시 토큰 무효할 시 액세스,리프래시 둘다 재발급", description = "토큰이 재발급 됩니다,")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리", content = @Content(examples = @ExampleObject(value = StringUtil.giveBothToken))),
            @ApiResponse(responseCode = "403", description = "권한 없음(다른 회원의 계정 변경)", content = @Content(examples = @ExampleObject(value = StringUtil.nulls))),
    })
    @PostMapping("/give-both-token")
    public ResponseEntity<?> giveBothToken(@RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        return ObjectResult.build("results", userService.giveBothToken(refreshTokenRequestDto));
    }
    @Operation(summary = "이메일로 회원 정보 찾기", description = "회원의 모든 정보를 찾아옵니다.", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리", content = @Content(examples = @ExampleObject(value = StringUtil.findUserInfo))),
            @ApiResponse(responseCode = "403", description = "권한 없음(다른 회원의 계정 변경)", content = @Content(examples = @ExampleObject(value = StringUtil.nulls))),
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user-info")
    public ResponseEntity<?> findUserInfo(@RequestParam(value = "userSeq") Long userSeq) {
        return ObjectResult.build("result", userService.getUserInfo(userSeq));
    }

    /**∑
     * 마이페이지 개인정보 수정
     *
     * @param changeUserInfoRequestDto
     * @return
     */
    @Operation(summary = "마이페이지 개인정보 수정", description = "회원의 정보를 수정합니다.", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리", content = @Content(examples = @ExampleObject(value = StringUtil.changeUserInfo))),
            @ApiResponse(responseCode = "403", description = "권한 없음(다른 회원의 계정 변경)", content = @Content(examples = @ExampleObject(value = StringUtil.nulls))),
    })
    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/user-info",consumes ={"multipart/form-data"})
    public ResponseEntity<?> changeUserInfo(@ModelAttribute ChangeUserInfoRequestDto changeUserInfoRequestDto) {

        userService.changeUserInfo(changeUserInfoRequestDto);
        return ObjectResult.ok();
    }

    /**
     * 필명 중복체크
     * @param userNickname
     * @return
     * @throws NoSuchAlgorithmException
     */
    @Operation(summary = "닉네임 중복 체크", description = "닉네임의 중복을 체크합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리", content = @Content(examples = @ExampleObject(value = StringUtil.checkNicknameDuplication))),
            @ApiResponse(responseCode = "403", description = "권한 없음(다른 회원의 계정 변경)", content = @Content(examples = @ExampleObject(value = StringUtil.nulls))),
    })
    @GetMapping("/nickname-check")
    public ResponseEntity<?> checkNicknameDuplication(@RequestParam(value = "userNickname") String userNickname) throws NoSuchAlgorithmException {
        return BooleanResult.build("result", userService.checkNicknameDuplication(userNickname));
    }

    /**
     * 키값으로 유저정보 조회
     * @param userSeq
     * @return
     */
    @Operation(summary = "키값으로 유저정보 조회", description = "회원의 정보를 조회합니다.", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리", content = @Content(examples = @ExampleObject(value = StringUtil.searchUserInfo))),
            @ApiResponse(responseCode = "403", description = "권한 없음(다른 회원의 계정 변경)", content = @Content(examples = @ExampleObject(value = StringUtil.nulls))),
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search-user-info")
    public ResponseEntity<?> searchUserInfo(@RequestParam(value = "userSeq")Long userSeq){
       return ObjectResult.build("results",userService.searchUserInfo(userSeq));
    }

    /**
     * 한글 초성 & 단어 검색 기능
     * @param userNickname
     * @param pageable
     * @return
     */
    @Operation(summary = "한글 초성 & 단어 검색 기능", description = "회원의 닉네임검색 기능입니다.", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리", content = @Content(examples = @ExampleObject(value = StringUtil.searchUserInitialList))),
            @ApiResponse(responseCode = "403", description = "권한 없음(다른 회원의 계정 변경)", content = @Content(examples = @ExampleObject(value = StringUtil.nulls))),
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search-user-list")
    public ResponseEntity<?> searchUserInitialList(@RequestParam(value = "userNickname")String userNickname, @PageableDefault(size = 30) Pageable pageable) {
        return PageResult.build(userService.searchUserList(userNickname,pageable));
    }

    /**
     * 푸시 알림 상태 변경
     * @param pushNotificationStatusDto
     * @return
     */
    @Operation(summary = "푸시 알림 상태 변경", description = "회원의 푸시 알림 상태를 변경합니다.", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리", content = @Content(examples = @ExampleObject(value = StringUtil.searchUserInitialList))),
            @ApiResponse(responseCode = "403", description = "권한 없음(다른 회원의 계정 변경)", content = @Content(examples = @ExampleObject(value = StringUtil.nulls))),
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/push-notification-change")
    public ResponseEntity<?> pushNotification(@RequestBody PushNotificationStatusDto pushNotificationStatusDto){
        userService.changePushNotification(pushNotificationStatusDto);
        return ObjectResult.ok();
    }

    /**
     * 푸시 알림 상태 조회
     * @param userSeq
     * @return
     */
    @Operation(summary = "푸시 알림 상태 조회", description = "회원의 푸시 알림 상태 조회합니다.", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리", content = @Content(examples = @ExampleObject(value = StringUtil.searchUserInitialList))),
            @ApiResponse(responseCode = "403", description = "권한 없음(다른 회원의 계정 변경)", content = @Content(examples = @ExampleObject(value = StringUtil.nulls))),
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/push-notification-info")
    public ResponseEntity<?> pushNotificationInfo(@RequestParam(value = "userSeq")Long userSeq){
      return ObjectResult.build("result",userService.userPushStatusInfo(userSeq));
    }
}
