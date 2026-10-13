package com.projects.kozip.address.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
public record AddressSearchResponse(
        String message,
        int count,
        List<AddressResponse> results) {
}
