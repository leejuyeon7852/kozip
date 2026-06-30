package com.projects.kozip.address.service;

import com.projects.kozip.address.dto.AddressResponse;
import com.projects.kozip.address.dto.AddressSearchResponse;
import com.projects.kozip.address.entity.RoadAddress;
import com.projects.kozip.address.repository.RoadAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoadAddressService {
    private final RoadAddressRepository repository;

    private static final int TOO_MANY_RESULTS = 1000;

    public AddressSearchResponse search(String query){
        if(repository.existsBySido(query) || repository.existsBySigungu(query)){
            return AddressSearchResponse.builder()
                    .message("더 자세하게 검색해주세요.")
                    .count(0)
                    .results(List.of())
                    .build();
        }

        List<RoadAddress> addresses = repository.search(query);
        List<AddressResponse> results = addresses.stream()
                .map(this::toResponse)
                .toList();

        String message = results.size() > TOO_MANY_RESULTS ? "검색 결과가 너무 많습니다." : null;

        return AddressSearchResponse.builder()
                .message(message)
                .count(results.size())
                .results(results)
                .build();
    }

    private AddressResponse toResponse(RoadAddress address) {
        String fullAddress = String.join(" ",
                address.getSido(),
                address.getSigungu() != null ? address.getSigungu() : "",
                address.getEupmyeondong(),
                address.getRoadName(),
                String.valueOf(address.getBuildingMain()),
                address.getBuildingSub() != 0 ? "-" + address.getBuildingSub() : "",
                address.getBuildingName() != null ? "(" + address.getBuildingName() + ")" : ""
        ).trim();

        return AddressResponse.builder()
                .zipCode(address.getZipCode())
                .address(fullAddress)
                .build();
    }
}
