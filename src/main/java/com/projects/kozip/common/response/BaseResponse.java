package com.projects.kozip.common.response;

public record BaseResponse<T>(
        boolean success,
        String code,
        String message,
        T data) {

    public static <T> BaseResponse<T> success(
            ResponseCode responseCode, T data) {

        return new BaseResponse<>(
                true,
                responseCode.name(),
                responseCode.message(),
                data);
    }

    public static <T> BaseResponse<T> failure(
            ResponseCode responseCode) {

        return new BaseResponse<>(
                false,
                responseCode.name(),
                responseCode.message(),
                null);
    }
}