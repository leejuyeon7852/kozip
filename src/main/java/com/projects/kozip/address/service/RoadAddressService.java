package com.projects.kozip.address.service;

import com.projects.kozip.address.dto.AddressResponse;
import com.projects.kozip.address.dto.AddressSearchResponse;
import com.projects.kozip.address.entity.RoadAddress;
import com.projects.kozip.address.repository.RoadAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoadAddressService {
    private final RoadAddressRepository repository;

    private static final int TOO_MANY_RESULTS = 1000;

    public AddressSearchResponse search(String query) {
        if (repository.existsBySidoContaining(query) || repository.existsBySigunguContaining(query)) {
            return AddressSearchResponse.builder()
                    .message("더 자세하게 검색해주세요.")
                    .count(0)
                    .results(List.of())
                    .build();
        }

        String[] parts = query.trim().split("\\s+");
        String keyword = parts[0];
        Integer buildingMain = null;

        if (parts.length > 1 && parts[parts.length - 1].matches("\\d+")) {
            buildingMain = Integer.parseInt(parts[parts.length - 1]);
            keyword = String.join(" ", Arrays.copyOf(parts, parts.length - 1));
        }

        long count = repository.countSearch(keyword, buildingMain);

        if (count > TOO_MANY_RESULTS) {
            return AddressSearchResponse.builder()
                    .message("검색 결과가 너무 많습니다. 더 자세하게 검색해주세요.")
                    .count((int) count)
                    .results(List.of())
                    .build();
        }

        List<RoadAddress> addresses = repository.search(keyword, buildingMain);
        List<AddressResponse> results = addresses.stream()
                .map(this::toResponse)
                .toList();

        return AddressSearchResponse.builder()
                .count(results.size())
                .results(results)
                .build();
    }

    private AddressResponse toResponse(RoadAddress address) {
        String building = address.getBuildingMain() +
                (address.getBuildingSub() != 0 ? "-" + address.getBuildingSub() : "");
        String jibun = address.getJibunMain() + (address.getJibunSub() != 0 ? "-" + address.getJibunSub() : "");

        String roadAddress = String.join(" ",
                address.getSido(),
                address.getSigungu() != null ? address.getSigungu() : "",
                address.getEupmyeondong(),
                address.getRoadName(),
                building
        ).replaceAll(" +", " ").trim();
        if (address.getBuildingName() != null) {
            roadAddress += " (" + address.getBuildingName() + ")";
        }

        String jibunAddress = String.join(" ",
                address.getSido(),
                address.getSigungu() != null ? address.getSigungu() : "",
                address.getEupmyeondong(),
                address.getRi() != null ? address.getRi() : "",
                jibun
        ).replaceAll(" +", " ").trim();

        return AddressResponse.builder()
                .zipCode(address.getZipCode())
                .roadAddress(roadAddress)
                .jibunAddress(jibunAddress)
                .build();
    }
}