package com.summar.summar.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.summar.summar.common.SummarCommonException;
import com.summar.summar.common.SummarErrorCode;
import com.summar.summar.domain.PushMessageResult;
import com.summar.summar.domain.User;
import com.summar.summar.dto.*;
import com.summar.summar.results.repository.PushMessageResultRepository;
import com.summar.summar.results.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;


@Service
@RequiredArgsConstructor
public class PushService {

    private final Logger log = LoggerFactory.getLogger(PushService.class);
    private final UserRepository userRepository;

    private final PushMessageResultRepository pushMessageResultRepository;
    @Value("${push.server-key}")
    private String serverKey;

    public void pushNotification(PushNotificationDto pushNotificationDto) {
        try {
            DataDto dataDto = new DataDto();
            ObjectMapper mapper = new ObjectMapper();
            Notification notification = Notification.builder()
                    .title(pushNotificationDto.getTitle())
                    .sound("default")
                    .body(pushNotificationDto.getBody())
                    .build();
            if(pushNotificationDto.getUserSeq() != null && pushNotificationDto.getPushType().equals("팔로우")){
                dataDto.setUserSeq(pushNotificationDto.getUserSeq());
                dataDto.setPushType(pushNotificationDto.getPushType());
            }
            if(pushNotificationDto.getUserSeq() != null && pushNotificationDto.getPushType().equals("좋아요")){
                dataDto.setUserSeq(pushNotificationDto.getUserSeq());
                dataDto.setFeedSeq(pushNotificationDto.getFeedSeq());
                dataDto.setPushType(pushNotificationDto.getPushType());
            }
            if(pushNotificationDto.getUserSeq() != null && (pushNotificationDto.getPushType().equals("댓글")|| pushNotificationDto.getPushType().equals("대댓글"))){
                dataDto.setUserSeq(pushNotificationDto.getUserSeq());
                dataDto.setFeedSeq(pushNotificationDto.getFeedSeq());
                dataDto.setFeedCommentSeq(pushNotificationDto.getFeedCommentSeq());
                dataDto.setPushType(pushNotificationDto.getPushType());
            }

            if(pushNotificationDto.getUserSeq() == null && pushNotificationDto.getFeedSeq() != null && pushNotificationDto.getFeedCommentSeq() != null){
                dataDto.setFeedSeq(pushNotificationDto.getFeedSeq());
                dataDto.setFeedCommentSeq(pushNotificationDto.getFeedCommentSeq());
                dataDto.setPushType(pushNotificationDto.getPushType());
            }

            //device token 정보 찾기
            User user = userRepository.findByUserNicknameAndLeaveYn(pushNotificationDto.getUserNickname(),false).orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));

            PushRequestDto pushRequestDto = PushRequestDto.builder()
                    .to(user.getDeviceToken())
                    .priority("high")//해당기기 디바이스 토큰
                    .notification(notification) //알림 - 제목,내용
                    .data(dataDto)
                    .build();
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setConnectTimeout(5000); //타임아웃 설정 5초
            factory.setReadTimeout(5000);//타임아웃 설정 5초

            RestTemplate restTemplate = new RestTemplate(factory);
            HttpHeaders header = new HttpHeaders();
            header.add("Content-Type", "application/json");
            header.add("Authorization", serverKey);

            String url = "https://fcm.googleapis.com/fcm/send"; //외부 api

            String pushRequestedDtoJson = mapper.writeValueAsString(pushRequestDto);

            HttpEntity<Object> request = new HttpEntity<>(pushRequestedDtoJson, header);

            PushMessageResultDto result = restTemplate.postForObject(new URL(url).toURI(), request, PushMessageResultDto.class);
            log.info("push-Notification result : {}", result);
            log.info("json result1 :   {}",request);
            log.info("json result2 :   {}",pushNotificationDto);

            pushMessageResultRepository.save(new PushMessageResult(result));

        } catch (MalformedURLException | URISyntaxException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }

}
