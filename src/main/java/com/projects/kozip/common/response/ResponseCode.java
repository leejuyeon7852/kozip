package com.projects.kozip.common.response;

import org.springframework.http.HttpStatus;

public enum ResponseCode {

    ADDRESS_SEARCH_OK(
            HttpStatus.OK,
            "주소 검색이 완료되었습니다."),

    ADDRESS_SEARCH_EMPTY(
            HttpStatus.OK,
            "검색 결과가 없습니다."),

    SEARCH_QUERY_TOO_SHORT(
            HttpStatus.BAD_REQUEST,
            "검색어는 2글자 이상 입력해주세요."),

    SEARCH_QUERY_TOO_BROAD(
            HttpStatus.BAD_REQUEST,
            "더 자세하게 검색해주세요."),

    SEARCH_TOO_MANY_RESULTS(
            HttpStatus.BAD_REQUEST,
            "검색 결과가 너무 많습니다. 더 자세하게 검색해주세요."),

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "요청 형식이 올바르지 않습니다."),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ResponseCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String message() {
        return message;
    }
}