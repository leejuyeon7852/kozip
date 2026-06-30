package com.projects.kozip.address.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AddressSearchResponse {
    private String message;
    private int count;
    private List<AddressResponse> results;
}
