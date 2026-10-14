package com.projects.kozip.address.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
public record AddressSearchResponse(
        int count,
        List<AddressResponse> results) {
}
