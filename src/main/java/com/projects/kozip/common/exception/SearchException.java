package com.projects.kozip.common.exception;

import com.projects.kozip.common.response.ResponseCode;

public class SearchException extends RuntimeException {

    private final ResponseCode responseCode;

    public SearchException(ResponseCode responseCode) {
        super(responseCode.message());
        this.responseCode = responseCode;
    }

    public ResponseCode responseCode() {
        return responseCode;
    }
}