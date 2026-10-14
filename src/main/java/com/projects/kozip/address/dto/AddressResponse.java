package com.projects.kozip.address.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
public record AddressResponse(
        String zipCode,
        String roadAddress,
        String jibunAddress) {
}
