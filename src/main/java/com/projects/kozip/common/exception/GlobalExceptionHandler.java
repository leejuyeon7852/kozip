package com.projects.kozip.common.exception;

import com.projects.kozip.common.response.BaseResponse;
import com.projects.kozip.common.response.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SearchException.class)
    public ResponseEntity<BaseResponse<Void>> handleSearchException(
            SearchException exception) {

        ResponseCode code = exception.responseCode();

        return ResponseEntity.status(code.httpStatus())
                .body(BaseResponse.<Void>failure(code));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<Void>> handleMissingParameter(
            MissingServletRequestParameterException exception) {

        ResponseCode code = ResponseCode.INVALID_REQUEST;

        return ResponseEntity.status(code.httpStatus())
                .body(BaseResponse.<Void>failure(code));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleUnexpectedException(
            Exception exception) {

        log.error("요청 처리 중 서버 오류 발생", exception);

        ResponseCode code = ResponseCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(code.httpStatus())
                .body(BaseResponse.<Void>failure(code));
    }
}