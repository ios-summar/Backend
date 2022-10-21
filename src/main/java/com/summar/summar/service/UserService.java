package com.summar.summar.service;

import com.summar.summar.dto.JoinRequestDto;
import com.summar.summar.mapper.UserMapper;
import com.summar.summar.repository.UserRepository;
import com.summar.summar.util.SHA256Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    @Transactional
    public Boolean saveUser(JoinRequestDto joinRequestDto) throws NoSuchAlgorithmException {
        //passwordEncoder 양방향 암호화 알고리즘 적용
        joinRequestDto.setUserHpNo(passwordEncoder.encode(joinRequestDto.getUserHpNo()));
        //SHA256 단방향 암호화 알고리즘 적용
        joinRequestDto.setUserPwd(SHA256Util.encrypt(joinRequestDto.getUserPwd()));
        userRepository.save(userMapper.toEntity(joinRequestDto));
        return true;
    }

    @Transactional
    public Boolean checkNicknameDuplication(String nickname) throws NoSuchAlgorithmException {
        return userRepository.existsByUserNickname(nickname);
    }

    @Transactional
    public Boolean checkUserIdDuplication(String userId) throws NoSuchAlgorithmException {
        return userRepository.existsByUserId(userId);
    }

}
