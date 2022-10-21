package com.summar.summar.results;

import com.summar.summar.dto.ApiResponseMessage;
import com.summar.summar.dto.PaginationInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Slf4j
public class ListResult {

    /**
     * 리스트 타입 ApiResult
     * @param resultListName 리스트 객체 이름
     * @param resultList 리스트 객체
     * @param paginationInfo 페이징 정보
     * @return
     */
    public static ResponseEntity<ListResult> build(String resultListName, List<ListResult> resultList, PaginationInfo paginationInfo) {

        ApiResult apiResult = ApiResult.blank()
                .add("totalRecordCount", paginationInfo.getTotalRecordCount())
                .add("totalPageCount", paginationInfo.getTotalPageCount())
                .add("firstPage", paginationInfo.getFirstPage())
                .add("lastPage", paginationInfo.getLastPage())
                .add("currentPageNo", paginationInfo.getPagingCriteria().getCurrentPageNo())
                .add("recordsPerPage", paginationInfo.getPagingCriteria().getRecordsPerPage())
                .add("pageSize", paginationInfo.getPagingCriteria().getPageSize())
                .add(resultListName, resultList);
        return (ResponseEntity<ListResult>) Result.ok(new ApiResponseMessage(apiResult));
    }

    /**
     * 리스트 타입 ApiResult
     * @param resultListName 리스트 객체 이름
     * @param resultList 리스트 객체
     * @return
     */
    public static ResponseEntity<ListResult> build(String resultListName, List<?> resultList) {

        ApiResult apiResult = ApiResult.blank()
                .add(resultListName, resultList);
        return (ResponseEntity<ListResult>) Result.ok(new ApiResponseMessage(apiResult));
    }
}
