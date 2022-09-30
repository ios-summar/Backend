package com.summar.gateway.controller;

import com.summar.gateway.auth.LoginUser;
import com.summar.gateway.auth.SummarUser;
import com.summar.gateway.common.CurrentUser;
import com.summar.gateway.dto.LoginRequestDto;
import com.summar.gateway.results.AuthenticationResult;
import com.summar.gateway.results.ListResult;
import com.summar.gateway.util.JwtUtil;
import com.summar.gateway.domain.User;
import com.summar.gateway.repository.UserRepository;
import com.summar.gateway.service.CustomUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1")
public class LoginController {
    private final CustomUserDetailService customUserDetailService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) throws Exception {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getUsername(),
                        loginRequestDto.getPassword())
        );

        final SummarUser customUser = (SummarUser) customUserDetailService.loadUserByUsername(loginRequestDto.getUsername());
        LoginUser loginUser = customUser.getLoginUser();
        //access token 생성
        final String accessToken = jwtUtil.generateToken(loginUser);
        //refresh token 생성
        final String refreshToken = jwtUtil.generateRefreshToken(loginUser);
        loginUser.setAccessToken(accessToken);
        loginUser.setRefreshToken(refreshToken);

        return AuthenticationResult.build(loginUser);
    }
    @PostMapping(value = "/test")
    public ResponseEntity<?> test(){
        List<String> list = new ArrayList<>();
        list.add("test1");
        list.add("test2");
        list.add("test3");
        list.add("test4");
        list.add("test5");
        return ListResult.build("results",list);
    }

    @PostMapping(value = "/useradd")
    public void USERADD() {
        User user = new User();
        user.setUserId("newy12");
        user.setUserPwd(passwordEncoder.encode("123"));
        userRepository.save(user);

    }

    //redis 연동 테스트
    @Cacheable(key = "#id" , cacheNames = "user")
    @GetMapping(value = "/redisadd")
    public void redisadd(){
        userRepository.findAll();
    }

   // @PreAuthorize("isAuthenticated()")
    @GetMapping("/good")
    public Map<String, String> good(@CurrentUser SummarUser user) {
        Map<String,String> map = new HashMap<>();
        map.put("userId",user.getLoginUser().getUserId());
        map.put("userpassword",user.getPassword());
        map.put("username",user.getUsername());
        return map;
    }
}
