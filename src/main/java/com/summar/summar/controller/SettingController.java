package com.summar.summar.controller;

import com.summar.summar.domain.Setting;
import com.summar.summar.enumeration.SettingType;
import com.summar.summar.repository.SettingRepository;
import com.summar.summar.results.ListResult;
import com.summar.summar.util.StringUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.webjars.NotFoundException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/setting")
public class SettingController {

    private final SettingRepository settingRepository;


    /**
     * 설정 페이지
     */
    @Operation(summary = "환경설정", description = "parameter = NOTICE 시 = 공지사항 관련 데이터조회,  " +
            "parameter = QUESTION 시 = 자주 묻는 질문 관련 데이터 조회", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리", content = @Content(examples = @ExampleObject(value = StringUtil.setting))),
            @ApiResponse(responseCode = "403", description = "권한 없음(다른 회원의 계정 변경)", content = @Content(examples = @ExampleObject(value = "\"result\":null"))),
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("")
    public ResponseEntity<?> setting(@RequestParam(value = "status")String status) {
        if("notice".equals(status)){
            return ListResult.build("results",settingRepository.findAllBySettingType(SettingType.NOTICE));
        }
        if("question".equals(status)){
            return ListResult.build("results",settingRepository.findAllBySettingType(SettingType.QUESTION));
        }
        else{
            throw new NotFoundException("없는 타입입니다.");
        }
    }
    @PostMapping("")
    public void test(){
        Setting setting = Setting.builder()
                .content("안녕하세요. 써머 회원님들 드디어 써머가 정식 오픈 합니다.")
                .settingType(SettingType.NOTICE)
                .status("new")
                .title("공지사항")
                .build();
        settingRepository.save(setting);
    }

}
