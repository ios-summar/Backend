package com.summar.summar.scheduler;

import com.summar.summar.domain.Setting;
import com.summar.summar.domain.User;
import com.summar.summar.dto.PushNotificationDto;
import com.summar.summar.enumeration.SettingType;
import com.summar.summar.results.repository.SettingRepository;
import com.summar.summar.results.repository.UserRepository;
import com.summar.summar.service.PushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SettingScheduler {

    private final SettingRepository settingRepository;
    private final PushService pushService;
    private final UserRepository userRepository;


    @Transactional
    @Scheduled(cron = "0 0/5 * * * *")
    public void questionCheck(){
        List<Setting> questions = settingRepository.findBySettingType(SettingType.QUESTION);
        for(Setting setting:questions){
            if("new".equals(setting.getStatus())){
                List<User> userInfos = userRepository.findAllByLeaveYn(false);
                for(User user:userInfos){
                    log.info("userNickname : {}",user.getUserNickname());
                    PushNotificationDto pushNotificationDto = PushNotificationDto.builder()
                            .title("Summar")
                            .body("새로운 자주 묻는 질문이 있어요. 확인해볼까요~?")
                            .userNickname(user.getUserNickname())
                            .build();
                    pushService.pushNotification(pushNotificationDto);
                }
                setting.setStatus(null);
                settingRepository.save(setting);
                log.info("ok");
            }
        }
    }
    @Transactional
    @Scheduled(cron = "0 0/5 * * * *")
    public void NoticeCheck(){
       List<Setting> notices  = settingRepository.findBySettingType(SettingType.NOTICE);
        for(Setting setting:notices){
            if("new".equals(setting.getStatus())){
                List<User> userInfos = userRepository.findAllByLeaveYn(false);
                for(User user:userInfos){
                    log.info("userNickname : {}",user.getUserNickname());
                    PushNotificationDto pushNotificationDto = PushNotificationDto.builder()
                            .title("Summar")
                            .body("새로운 공지사항이 있어요. 확인해볼까요~?")
                            .userNickname(user.getUserNickname())
                            .build();
                    pushService.pushNotification(pushNotificationDto);
                }
                setting.setStatus(null);
                settingRepository.save(setting);
                log.info("ok");
            }
        }
    }
}
