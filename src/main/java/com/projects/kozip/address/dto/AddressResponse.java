package com.projects.kozip.address.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressResponse {
    private String zipCode;
    private String roadAddress;
    private String jibunAddress;
}
