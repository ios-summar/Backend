package com.summar.summar.results;

import com.summar.summar.dto.ApiResponseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Slf4j
public class BooleanResult {

    public static ResponseEntity<BooleanResult> build(String resultName,boolean resultStatus,  String message , List<String> messageResult) {

        ApiResult apiResult = ApiResult.blank()
                .add(resultName, resultStatus)
                        .add(message,messageResult);
        return (ResponseEntity<BooleanResult>) Result.ok(new ApiResponseMessage(apiResult));
    }

    public static ResponseEntity<BooleanResult> build(String resultName,boolean resultStatus) {

        ApiResult apiResult = ApiResult.blank()
                .add(resultName, resultStatus);
        return (ResponseEntity<BooleanResult>) Result.ok(new ApiResponseMessage(apiResult));
    }
}
