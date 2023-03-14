package com.summar.summar.dto;

import com.summar.summar.domain.Feed;
import com.summar.summar.domain.FeedComment;
import com.summar.summar.domain.User;
import com.summar.summar.enumeration.NotificationType;
import lombok.Builder;
import lombok.Data;



@Data
@Builder
public class GatheringNotificationSaveDto {

    //내계정 seq
    private User userSeq;
    //상대계정 seq
    private User otherUserSeq;
    private Feed feed;
    private FeedComment feedComment;
    //알림타입
    private NotificationType notificationType;
    private Boolean delYn;

}
