package com.summar.summar.service;

import com.summar.summar.domain.User;
import com.summar.summar.enumeration.UserStatus;
import com.summar.summar.results.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SleepUserService {

    private final UserRepository userRepository;


    @Transactional
    //@Scheduled(cron = "0 0 0 * * *")
    //@Scheduled(cron = "5 * * * * *")
    public void sleepUser(){
        List<User> user =  userRepository.findAll();
        if(!ObjectUtils.isEmpty(user)){
            for (User userInfo: user) {
                long betweenDays = Duration.between(userInfo.getLastLoginDate().atStartOfDay(),LocalDate.now().atStartOfDay()).toDays();
                log.info("최초 로그인 이후 접속하지 않은 회원 정보 => 회원아이디 : {} 날짜 : {}",userInfo.getUserEmail() ,betweenDays);
                if(betweenDays >= 365){
                    userInfo.setUserStatus(UserStatus.sleep);
                    log.info("휴면전환 아이디 : {}",userInfo.getUserEmail());
                    userRepository.save(userInfo);
                }
            }
        }
    }
}
