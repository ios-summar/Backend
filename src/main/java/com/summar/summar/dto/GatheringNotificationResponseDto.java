package com.summar.summar.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.summar.summar.domain.GatheringNotification;
import com.summar.summar.enumeration.NotificationType;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;


import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class GatheringNotificationResponseDto implements Serializable {

    private Long gatheringNotificationSeq;
    //내용
    private String content;
    //내계정 seq
    private Long userSeq;
    //상대계정 seq
    private Long otherUserSeq;

    private Long feedSeq;

    private Long feedCommentSeq;

    //이미지Url
    private String imageUrl;
    //피드이미지Url
    private String feedImageUrl;
    //알림타입
    private NotificationType notificationType;
    private Boolean followCheck;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    public GatheringNotificationResponseDto(GatheringNotification gatheringNotification){
        this.gatheringNotificationSeq = gatheringNotification.getGatheringNotificationSeq();
        this.userSeq = gatheringNotification.getUserSeq().getUserSeq();
        this.otherUserSeq = gatheringNotification.getOtherUserSeq().getUserSeq();
        this.notificationType = gatheringNotification.getNotificationType();
        this.content = gatheringNotification.getOtherUserSeq().getUserNickname();
        switch (gatheringNotification.getNotificationType()){
            case 댓글: content += "님이 회원님의 피드에 댓글을 달았어요.";break;
            case 좋아요: content += "님이 회원님의 피드를 좋아합니다.";break;
            case 팔로우: content += "님이 회원님을 팔로우 했어요.";break;
        }
        this.createdDate = gatheringNotification.getCreatedDate();
        this.imageUrl = gatheringNotification.getOtherUserSeq().getProfileImageUrl();
        if(gatheringNotification.getFeed()!=null){
            this.feedSeq = gatheringNotification.getFeed().getFeedSeq();
            this.feedImageUrl = gatheringNotification.getFeed().getFeedImages().get(0).getImageUrl();
            if(gatheringNotification.getFeedComment()!=null){
                this.feedCommentSeq = gatheringNotification.getFeedComment().getFeedCommentSeq();
                String comment = gatheringNotification.getFeedComment().getComment();
                if(comment.length()>10){
                    comment = comment.substring(0,11) + "...";
                }
                this.content += "\""+comment+"\"";
            }else{
                this.feedCommentSeq = 0L;
            }
        }else{
            this.feedSeq = 0L;
            this.feedImageUrl = null;
        }
        this.followCheck = gatheringNotification.getFollowCheck();
    }
}
