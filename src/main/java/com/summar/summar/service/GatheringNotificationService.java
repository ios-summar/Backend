package com.summar.summar.service;


import com.summar.summar.common.SummarCommonException;
import com.summar.summar.common.SummarErrorCode;
import com.summar.summar.domain.GatheringNotification;
import com.summar.summar.domain.User;
import com.summar.summar.dto.GatheringNotificationResponseDto;
import com.summar.summar.repository.GatheringNotificationRepository;
import com.summar.summar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GatheringNotificationService {

    private final GatheringNotificationRepository gatheringNotificationRepository;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public List<GatheringNotificationResponseDto> findByNotificationList(Long userSeq) {
        User userInfo = userRepository.findById(userSeq).orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        List<GatheringNotification> gatheringNotificationList = gatheringNotificationRepository.findAllByUserSeq(userInfo);

        return gatheringNotificationList
                .stream()
                .map(GatheringNotificationResponseDto::new)
                .collect(Collectors.toList());
    }
}
