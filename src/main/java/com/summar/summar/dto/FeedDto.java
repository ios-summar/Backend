package com.summar.summar.dto;


import com.summar.summar.domain.FeedImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;


/**
 * A DTO for the {@link com.summar.summar.domain.User} entity.
 */

@Getter
@Setter
@Builder
public class FeedDto implements Serializable {


    @Schema(description = "피드 시퀀스")
    private Long feedSeq;

    @Schema(description = "유저 정보")
    private SimpleUserVO user;

    @Schema(description = "피드 내용")
    private String contents;

    @Schema(description = "피드 이미지 리스트")
    private List<FeedImage> feedImages;

    @Schema(description = "비공개 여부")
    private boolean secretYn;

    @Schema(description = "댓글 허용 여부")
    private boolean commentYn;

    @Schema(description = "피드 임시 저장 여부")
    private boolean tempSaveYn;

    @Schema(description = "해당 피드 좋아요 여부")
    private boolean likeYn;

    @Schema(description = "해당 피드 좋아요 수")
    private int totalLikeCount;

    @Schema(description = "해당 피드 스크랩 여부")
    private boolean scrapYn;

    @Schema(description = "해당 피드 댓글 수(대댓글 포함)")
    private int totalCommentCount;

    @Schema(description = "활성화 여부, true:활성화, false:삭제")
    private boolean activated;

    @Schema(description = "마지막 수정 날짜")
    private ZonedDateTime lastModifiedDate;

    @Schema(description = "최초 생성 날짜")
    private ZonedDateTime createdDate;

}
