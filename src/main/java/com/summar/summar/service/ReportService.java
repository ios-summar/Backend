package com.summar.summar.service;


import com.summar.summar.common.SummarCommonException;
import com.summar.summar.common.SummarErrorCode;
import com.summar.summar.domain.Feed;
import com.summar.summar.domain.FeedComment;
import com.summar.summar.domain.Report;
import com.summar.summar.domain.User;
import com.summar.summar.dto.ReportRequestDto;
import com.summar.summar.dto.ReportSaveDto;
import com.summar.summar.results.repository.FeedCommentRepository;
import com.summar.summar.results.repository.FeedRepository;
import com.summar.summar.results.repository.ReportRepository;
import com.summar.summar.results.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final FeedCommentRepository feedCommentRepository;

    @Transactional
    public boolean insertReport(ReportRequestDto reportRequestDto) {
        User user = userRepository.findById(reportRequestDto.getMySeq())
                .orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        User otherUser = userRepository.findById(reportRequestDto.getUserSeq())
                .orElseThrow(() -> new SummarCommonException(SummarErrorCode.USER_NOT_FOUND.getCode(), SummarErrorCode.USER_NOT_FOUND.getMessage()));
        Feed feed = feedRepository.findById(reportRequestDto.getFeedSeq()).orElse(null);
        FeedComment feedComment = feedCommentRepository.findById(reportRequestDto.getFeedCommentSeq()).orElse(null);

        ReportSaveDto reportSaveDto = ReportSaveDto.builder()
                .mySeq(user)
                .userSeq(otherUser)
                .feedSeq(feed)
                .feedCommentSeq(feedComment)
                .reportContent(reportRequestDto.getReportContent())
                .reportType(reportRequestDto.getReportType())
                .build();
        reportRepository.save(new Report(reportSaveDto));
        return true;
    }
}
