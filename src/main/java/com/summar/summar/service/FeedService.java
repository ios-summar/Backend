package com.summar.summar.service;

import com.summar.summar.common.SummarCommonException;
import com.summar.summar.common.SummarErrorCode;
import com.summar.summar.domain.*;
import com.summar.summar.dto.*;
import com.summar.summar.enumeration.NotificationType;
import com.summar.summar.repository.*;
import com.summar.summar.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class FeedService {

    private final FeedRepository feedRepository;
    private final FeedImageRepository feedImageRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedScrapRepository feedScrapRepository;

    private final FeedCommentRepository feedCommentRepository;

    private final GatheringNotificationRepository gatheringNotificationRepository;
    private final UserRepository userRepository;

    private final S3Service s3Service;

    private final PushService pushService;

    private final JwtUtil jwtUtil;

    @Transactional
    public FeedDto saveFeed(FeedRegisterDto feedRegisterDto) {
        User user = userRepository.findById(feedRegisterDto.getUserSeq()).get();
        Feed feed = new Feed(feedRegisterDto,user);
        Long feedSeq = feedRepository.save(feed).getFeedSeq();
        if(feedRegisterDto.getImages()!=null) {
            feedRegisterDto.getImages().forEach(
                    image -> {
                        String feedImg = s3Service.upload(image, S3Service.FEED_IMAGE);
                        FeedImage feedImage = new FeedImage(feedSeq, feedImg.replace("https", "http")
                                , feedRegisterDto.getImages().indexOf(image), feed, true);
                        feedImageRepository.save(feedImage);
                    });
        }
        SimpleUserVO simpleUserVO = new SimpleUserVO(userRepository.findById(feedRegisterDto.getUserSeq()).get());
        return FeedDto.builder()
                .feedSeq(feedSeq)
                .feedImages(feedImageRepository.findByFeedSeqAndActivatedIsTrue(feedSeq))
                .user(simpleUserVO)
                .contents(feedRegisterDto.getContents())
                .commentYn(feedRegisterDto.isCommentYn())
                .tempSaveYn(feedRegisterDto.isTempSaveYn())
                .secretYn(feedRegisterDto.isSecretYn())
                .build();
    }

    @Transactional
    public FeedDto updateFeed(FeedUpdateDto feedUpdateDto){
        Long feedSeq = feedUpdateDto.getFeedSeq();
        Feed feed = feedRepository.findOneByFeedSeq(feedSeq);
        if(!feed.isTempSaveYn() && feedUpdateDto.isTempSaveYn()){
            throw new SummarCommonException(SummarErrorCode.INVALID_TEMP_SAVE.getCode(), SummarErrorCode.INVALID_TEMP_SAVE.getMessage());
        }
        if(feedUpdateDto.getDeleteImageSeqs()!=null){
            feedImageRepository.findAllById(feedUpdateDto.getDeleteImageSeqs())
                    .forEach(feedImage -> feedImage.setActivated(false));
        }
        if(feedUpdateDto.getInsertImages()!=null){
            feedUpdateDto.getInsertImages().forEach(
                    image -> {
                        String feedImg = s3Service.upload(image, S3Service.FEED_IMAGE);
                        FeedImage feedImage = new FeedImage(feedSeq, feedImg.replace("https", "http")
                                , feedUpdateDto.getInsertImages().indexOf(image), feed, true);
                        feedImageRepository.save(feedImage);
                    });
        }
        feedRepository.save(feed);
        return FeedDto.builder()
                .feedSeq(feedSeq)
                .feedImages(feedImageRepository.findByFeedSeqAndActivatedIsTrue(feedSeq))
                .user(new SimpleUserVO(feed.getUser()))
                .contents(feedUpdateDto.getContents())
                .commentYn(feedUpdateDto.isCommentYn())
                .tempSaveYn(feedUpdateDto.isTempSaveYn())
                .secretYn(feedUpdateDto.isSecretYn())
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<FeedDto> getFeedByFeedSeq(Long feedSeq) {
        Optional<Feed> feed = feedRepository.findById(feedSeq);
        return Optional.ofNullable(FeedDto.builder()
                .feedSeq(feedSeq)
                .feedImages(feedImageRepository.findByFeedSeqAndActivatedIsTrue(feedSeq))
                .user(new SimpleUserVO(feed.get().getUser()))
                .contents(feed.get().getContents())
                .commentYn(feed.get().isCommentYn())
                .tempSaveYn(feed.get().isTempSaveYn())
                .secretYn(feed.get().isSecretYn())
                .likeYn(feedLikeRepository.existsByActivatedIsTrueAndFeedFeedSeqAndUserUserSeq(feedSeq,jwtUtil.getCurrentUserSeq()))
                .totalLikeCount(feedLikeRepository.findByFeedFeedSeqAndActivatedIsTrue(feedSeq).get().size())
                .scrapYn(feedScrapRepository.existsByActivatedIsTrueAndFeedFeedSeqAndUserUserSeq(feedSeq,jwtUtil.getCurrentUserSeq()))
                .totalCommentCount(feedCommentRepository.findAllByFeedFeedSeq(feedSeq).size())
                .activated(feed.get().isActivated())
                .lastModifiedDate(feed.get().getModifiedDate())
                .createdDate(feed.get().getCreatedDate())
                .build());
    }

    @Transactional(readOnly = true)
    public Page<FeedDto> getFeed(Pageable page) {
        Page<Feed> feeds = feedRepository.findAllByActivatedIsTrueAndSecretYnIsFalseAndTempSaveYnIsFalseAndUserLeaveYnIsFalse(page);
        List<FeedDto> feedDtos = new ArrayList<>();
        feeds.forEach(
                feed -> feedDtos.add(FeedDto.builder()
                    .feedSeq(feed.getFeedSeq())
                    .feedImages(feedImageRepository.findByFeedSeqAndActivatedIsTrue(feed.getFeedSeq()))
                    .user(new SimpleUserVO(feed.getUser()))
                    .contents(feed.getContents())
                    .activated(feed.isActivated())
                    .tempSaveYn(feed.isTempSaveYn())
                    .secretYn(feed.isSecretYn())
                    .commentYn(feed.isCommentYn())
                    .likeYn(feedLikeRepository.existsByActivatedIsTrueAndFeedFeedSeqAndUserUserSeq(feed.getFeedSeq(),jwtUtil.getCurrentUserSeq()))
                    .totalLikeCount(feedLikeRepository.findByFeedFeedSeqAndActivatedIsTrue(feed.getFeedSeq()).get().size())
                    .scrapYn(feedScrapRepository.existsByActivatedIsTrueAndFeedFeedSeqAndUserUserSeq(feed.getFeedSeq(),jwtUtil.getCurrentUserSeq()))
                    .totalCommentCount(feedCommentRepository.findAllByFeedFeedSeq(feed.getFeedSeq()).size())
                    .lastModifiedDate(feed.getModifiedDate())
                    .createdDate(feed.getCreatedDate())
                    .build()));
        return new PageImpl<>(feedDtos,page,feeds.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<FeedDto> getTempFeed(Long userSeq,Pageable page) {
        List<FeedDto> feedDtos = new ArrayList<>();
        if(userSeq.equals(jwtUtil.getCurrentUserSeq())){
            Page<Feed> feeds = feedRepository.findAllByActivatedIsTrueAndTempSaveYnIsTrueAndUserUserSeq(page,userSeq);
            feeds.forEach(
                    feed -> feedDtos.add(FeedDto.builder()
                            .feedSeq(feed.getFeedSeq())
                            .feedImages(feedImageRepository.findByFeedSeqAndActivatedIsTrue(feed.getFeedSeq()))
                            .user(new SimpleUserVO(feed.getUser()))
                            .contents(feed.getContents())
                            .activated(feed.isActivated())
                            .tempSaveYn(feed.isTempSaveYn())
                            .secretYn(feed.isSecretYn())
                            .commentYn(feed.isCommentYn())
                            .lastModifiedDate(feed.getModifiedDate())
                            .createdDate(feed.getCreatedDate())
                            .build()));
            return new PageImpl<>(feedDtos,page,feeds.getTotalElements());
        }else{
            return new PageImpl<>(feedDtos,page,0);
        }
    }

    @Transactional(readOnly = true)
    public Page<FeedDto> getFeedByUserSeq(Long userSeq,Pageable page) {
        Page<Feed> feeds;
        if(userSeq.equals(jwtUtil.getCurrentUserSeq())){
            feeds = feedRepository.findAllByActivatedIsTrueAndUserUserSeqAndTempSaveYnIsFalse(userSeq,page);
        }else{
            feeds = feedRepository.findAllByActivatedIsTrueAndSecretYnIsFalseAndTempSaveYnIsFalseAndUserUserSeq(userSeq,page);
        }
        List<FeedDto> feedDtos = new ArrayList<>();
        feeds.forEach(
                feed -> feedDtos.add(FeedDto.builder()
                        .feedSeq(feed.getFeedSeq())
                        .feedImages(feedImageRepository.findByFeedSeqAndActivatedIsTrue(feed.getFeedSeq()))
                        .user(new SimpleUserVO(feed.getUser()))
                        .contents(feed.getContents())
                        .activated(feed.isActivated())
                        .tempSaveYn(feed.isTempSaveYn())
                        .secretYn(feed.isSecretYn())
                        .commentYn(feed.isCommentYn())
                        .likeYn(feedLikeRepository.existsByActivatedIsTrueAndFeedFeedSeqAndUserUserSeq(feed.getFeedSeq(),jwtUtil.getCurrentUserSeq()))
                        .totalLikeCount(feedLikeRepository.findByFeedFeedSeqAndActivatedIsTrue(feed.getFeedSeq()).get().size())
                        .scrapYn(feedScrapRepository.existsByActivatedIsTrueAndFeedFeedSeqAndUserUserSeq(feed.getFeedSeq(),jwtUtil.getCurrentUserSeq()))
                        .totalCommentCount(feedCommentRepository.findAllByFeedFeedSeq(feed.getFeedSeq()).size())
                        .lastModifiedDate(feed.getModifiedDate())
                        .createdDate(feed.getCreatedDate())
                        .build()));
        return new PageImpl<>(feedDtos,page,feeds.getTotalElements());
    }

    @Transactional
    public FeedDto updateFeedInActivated(Long feedSeq) {
        Feed feed = feedRepository.findOneByFeedSeq(feedSeq);
        feed.setActivated(false);
        return FeedDto.builder()
                .feedSeq(feedSeq)
                .feedImages(feedImageRepository.findByFeedSeqAndActivatedIsTrue(feedSeq))
                .user(new SimpleUserVO(feed.getUser()))
                .contents(feed.getContents())
                .commentYn(feed.isCommentYn())
                .tempSaveYn(feed.isTempSaveYn())
                .secretYn(feed.isSecretYn())
                .activated(feed.isActivated())
                .lastModifiedDate(feed.getModifiedDate())
                .createdDate(feed.getCreatedDate())
                .build();
    }

    @Transactional
    public Boolean setFeedLike(Long feedSeq, FeedLikeDto feedLikeDto){

        Optional<FeedLike> feedLike =feedLikeRepository.findByFeedFeedSeqAndUserUserSeq(feedSeq, feedLikeDto.getUserSeq());
        Feed feed = feedRepository.findOneByFeedSeq(feedSeq);
        User user = userRepository.findByUserSeqAndLeaveYn(feedLikeDto.getUserSeq(),false).get();
        feedLike.ifPresentOrElse(
                findFeed ->{
                    findFeed.setActivated(!findFeed.isActivated());
                    if(!feed.getUser().getUserSeq().equals(jwtUtil.getCurrentUserSeq())) {
                        if (findFeed.isActivated()) {
                            PushNotificationDto pushNotificationDto = PushNotificationDto.builder()
                                    .title("Summar")
                                    .body(user.getUserNickname() + "님이 회원님의 피드를 좋아합니다.")
                                    .userNickname(feed.getUser().getUserNickname())
                                    .build();
                            GatheringNotification gatheringNotification = new GatheringNotification(
                                    GatheringNotificationSaveDto.builder()
                                            .content(pushNotificationDto.getBody())
                                            .userSeq(user)
                                            .otherUserSeq(feed.getUser())
                                            .imageUrl(user.getProfileImageUrl())
                                            //처음꺼 피드이미지 노출
                                            .feedImageUrl(findFeed.getFeed().getFeedImages().get(0).getImageUrl())
                                            .notificationType(NotificationType.좋아요)
                                            .build());
                            gatheringNotificationRepository.save(gatheringNotification);
                            pushService.pushNotification(pushNotificationDto);
                        }
                    }
                },
                ()-> {
                    userRepository.findByUserSeqAndLeaveYn(feedLikeDto.getUserSeq(),false).get();
                    FeedLike newLike = new FeedLike(feed,user,true);
                    feedLikeRepository.save(newLike);

                    PushNotificationDto pushNotificationDto = PushNotificationDto.builder()
                            .title("Summar")
                            .body(user.getUserNickname() + "님이 회원님의 피드를 좋아합니다.")
                            .userNickname(feed.getUser().getUserNickname())
                            .build();
                    GatheringNotification gatheringNotification = new GatheringNotification(
                            GatheringNotificationSaveDto.builder()
                                    .content(pushNotificationDto.getBody())
                                    .userSeq(user)
                                    .otherUserSeq(feed.getUser())
                                    .imageUrl(user.getProfileImageUrl())
                                    //처음꺼 피드이미지 노출
                                    .feedImageUrl(feed.getFeedImages().get(0).getImageUrl())
                                    .notificationType(NotificationType.좋아요)
                                    .build());
                    gatheringNotificationRepository.save(gatheringNotification);
                    pushService.pushNotification(pushNotificationDto);
                });



        return true;
    }

    @Transactional(readOnly = true)
    public Page<FeedCommentDto> getFeedCommentsByFeedSeq(Pageable page, Long feedSeq) {
        Page<FeedComment> feedComments = feedCommentRepository.findAllByFeedFeedSeq(feedSeq,page);
        List<FeedCommentDto> feedCommentDtos = new ArrayList<>();
        List<FeedComment> parentComments = feedComments.stream().filter(feedComment1 -> feedComment1.getParentCommentSeq().equals(0L))
                .collect(Collectors.toList());
        List<FeedComment> childComments = feedComments.stream().filter(feedComment1 -> !feedComment1.getParentCommentSeq().equals(0L))
                .collect(Collectors.toList());
        parentComments.forEach(
                parentComment -> {
                    List<FeedCommentDto> myChildrenComments = new ArrayList<>();
                    childComments.stream().filter(childComment->childComment.getParentCommentSeq().equals(parentComment.getFeedCommentSeq())).collect(Collectors.toList())
                            .forEach(
                                    myChild ->{
                                        FeedCommentDto myChildren = new FeedCommentDto(myChild.getFeedCommentSeq()
                                                , myChild.getCreatedDate()
                                                , myChild.getFeed().getFeedSeq(), new SimpleUserVO(myChild.getUser()), myChild.isActivated()
                                                , null,0, myChild.getModifiedDate(), myChild.getComment());
                                        myChildrenComments.add(myChildren);
                                    });
                    feedCommentDtos.add(new FeedCommentDto(parentComment.getFeedCommentSeq()
                            , parentComment.getCreatedDate()
                            , parentComment.getFeed().getFeedSeq(), new SimpleUserVO(parentComment.getUser()), parentComment.isActivated()
                            , myChildrenComments, myChildrenComments.size(), parentComment.getModifiedDate()
                            , parentComment.getComment()));
                });
        return new PageImpl<>(feedCommentDtos,page,feedComments.getTotalElements());
    }

    @Transactional
    public void saveFeedComment(FeedCommentRegisterDto feedCommentRegisterDto) {
        User user = userRepository.findById(feedCommentRegisterDto.getUserSeq()).get();
        Feed feed = feedRepository.findOneByFeedSeq(feedCommentRegisterDto.getFeedSeq());
        FeedComment feedComment = new FeedComment(feedCommentRegisterDto,feed,user);
        feedCommentRepository.save(feedComment);

        if(!feed.getUser().getUserSeq().equals(jwtUtil.getCurrentUserSeq())){
            PushNotificationDto pushNotificationDto = PushNotificationDto.builder()
                    .title("Summar")
                    .body(user.getUserNickname() + "님이 회원님의 피드에 댓글을 달았어요.")
                    .userNickname(feed.getUser().getUserNickname())
                    .build();

            pushService.pushNotification(pushNotificationDto);

            GatheringNotification gatheringNotification = new GatheringNotification(
                    GatheringNotificationSaveDto.builder()
                            .content(pushNotificationDto.getBody())
                            .userSeq(user)
                            .otherUserSeq(feed.getUser())
                            .imageUrl(user.getProfileImageUrl())
                            //처음꺼 피드이미지 노출
                            .feedImageUrl(feed.getFeedImages().get(0).getImageUrl())
                            .notificationType(NotificationType.댓글)
                            .build());
            gatheringNotificationRepository.save(gatheringNotification);
        }

    }

    @Transactional
    public void updateFeedCommentInActivated(Long feedCommentSeq) {
        FeedComment feedComment = feedCommentRepository.findOneByFeedCommentSeq(feedCommentSeq);
        feedComment.setActivated(false);
    }

    @Transactional
    public void updateFeedComment(FeedCommentUpdateDto feedCommentUpdateDto) {
        FeedComment feedComment = feedCommentRepository.findOneByFeedCommentSeq(feedCommentUpdateDto.getFeedCommentSeq());
        feedComment.setComment(feedCommentUpdateDto.getComment());
    }

    @Transactional(readOnly = true)
    public Page<FeedDto> getFeedScrap(Pageable page) {
        Long userSeq = jwtUtil.getCurrentUserSeq();
        Page<FeedScrap> feedScraps = feedScrapRepository.findByUserUserSeqAndActivatedIsTrueAndFeedActivatedIsTrue(page,userSeq);
        List<Long> feedScrapIds = feedScraps.stream().map(feedScrap -> feedScrap.getFeed().getFeedSeq()).collect(Collectors.toList());
        Page<Feed> feeds = feedRepository.findByFeedSeqIn(page,feedScrapIds);
        List<FeedDto> feedDtos = new ArrayList<>();
        feeds.forEach(
                feed -> feedDtos.add(FeedDto.builder()
                        .feedSeq(feed.getFeedSeq())
                        .feedImages(feedImageRepository.findByFeedSeqAndActivatedIsTrue(feed.getFeedSeq()))
                        .user(new SimpleUserVO(feed.getUser()))
                        .contents(feed.getContents())
                        .activated(feed.isActivated())
                        .tempSaveYn(feed.isTempSaveYn())
                        .secretYn(feed.isSecretYn())
                        .commentYn(feed.isCommentYn())
                        .lastModifiedDate(feed.getModifiedDate())
                        .createdDate(feed.getCreatedDate())
                        .likeYn(feedLikeRepository.existsByActivatedIsTrueAndFeedFeedSeqAndUserUserSeq(feed.getFeedSeq(),jwtUtil.getCurrentUserSeq()))
                        .totalLikeCount(feedLikeRepository.findByFeedFeedSeqAndActivatedIsTrue(feed.getFeedSeq()).get().size())
                        .scrapYn(feedScrapRepository.existsByActivatedIsTrueAndFeedFeedSeqAndUserUserSeq(feed.getFeedSeq(),jwtUtil.getCurrentUserSeq()))
                        .totalCommentCount(feedCommentRepository.findAllByFeedFeedSeq(feed.getFeedSeq()).size())
                        .build()));
        return new PageImpl<>(feedDtos,page,feeds.getTotalElements());
    }

    @Transactional
    public Boolean setFeedScrap(Long feedSeq, FeedScrapDto feedScrapDto){
        Optional<FeedScrap> feedScrap =feedScrapRepository.findByFeedFeedSeqAndUserUserSeq(feedSeq, feedScrapDto.getUserSeq());
        feedScrap.ifPresentOrElse(
                findFeed -> findFeed.setActivated(!findFeed.isActivated()),
                ()-> {
                    Feed feed = feedRepository.findOneByFeedSeq(feedSeq);
                    User user = userRepository.findByUserSeqAndLeaveYn(feedScrapDto.getUserSeq(),false).get();
                    FeedScrap newScrap = new FeedScrap(feed,user,true);
                    feedScrapRepository.save(newScrap);
                });
        return true;
    }
}
