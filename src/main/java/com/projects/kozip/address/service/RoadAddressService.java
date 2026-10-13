package com.projects.kozip.address.service;

import com.projects.kozip.address.dto.AddressResponse;
import com.projects.kozip.address.dto.AddressSearchResponse;
import com.projects.kozip.address.dto.search.FullTextInput;
import com.projects.kozip.address.dto.search.RegionInput;
import com.projects.kozip.address.dto.search.RoadInput;
import com.projects.kozip.address.entity.RoadAddress;
import com.projects.kozip.address.repository.RoadAddressRepository;
import com.projects.kozip.address.search.AddressQueryParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoadAddressService {
    private final RoadAddressRepository repository;
    private final AddressQueryParser queryParser;

    private static final int TOO_MANY_RESULTS = 1000;

    public AddressSearchResponse search(String query) {
        String trimmed = query.trim();

        if (trimmed.length() < 2) {
            return AddressSearchResponse.builder()
                    .message("검색어는 2글자 이상 입력해주세요.")
                    .count(0)
                    .results(List.of())
                    .build();
        }

        if (queryParser.isZipCode(trimmed)) {
            return searchByZipCode(trimmed);
        }

        RegionInput region = queryParser.parseRegion(trimmed);

        long regionStart = System.nanoTime();

        boolean regionOnly = region.isSidoOnly()
                || (region.isSigunguCandidate()
                && repository.existsBySigungu(region.remaining()));

        log.info("검색 단계: 지역 검사, 소요시간: {}ms",
                (System.nanoTime() - regionStart) / 1_000_000);

        if (regionOnly) {
            return AddressSearchResponse.builder()
                    .message("더 자세하게 검색해주세요.")
                    .count(0)
                    .results(List.of())
                    .build();
        }

        String[] parts = region.parts();
        RoadInput roadInput = queryParser.parseRoad(parts);

        if (roadInput != null) {
            long roadStart = System.nanoTime();

            List<RoadAddress> addresses = repository.searchByRoad(
                    roadInput.roadName(),
                    region.sido(),
                    roadInput.sigungu(),
                    roadInput.buildingMain(),
                    roadInput.buildingSub());

            log.info("검색 단계: 도로명 조회, 결과: {}, 소요시간: {}ms",
                    addresses.size(),
                    (System.nanoTime() - roadStart) / 1_000_000);

            return toSearchResponse(addresses);
        }

        FullTextInput fullTextInput = queryParser.parseFullText(parts);

        if (fullTextInput.isTooShort()) {
            return AddressSearchResponse.builder()
                    .message("검색어는 2글자 이상 입력해주세요.")
                    .count(0)
                    .results(List.of())
                    .build();
        }

        long searchStart = System.nanoTime();

        List<RoadAddress> addresses = repository.search(
                fullTextInput.matchKeyword(),
                fullTextInput.buildingMain(),
                region.sido());

        log.info("검색 단계: FULLTEXT 결과 조회, 결과: {}, 소요시간: {}ms",
                addresses.size(),
                (System.nanoTime() - searchStart) / 1_000_000);

        return toSearchResponse(addresses);
    }

    private AddressSearchResponse searchByZipCode(String zipCode) {
        List<RoadAddress> addresses = repository.findByZipCode(zipCode);
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

    private AddressSearchResponse toSearchResponse(
            List<RoadAddress> addresses) {

        if (addresses.size() > TOO_MANY_RESULTS) {
            return AddressSearchResponse.builder()
                    .message("검색 결과가 너무 많습니다. 더 자세하게 검색해주세요.")
                    .count(addresses.size())
                    .results(List.of())
                    .build();
        }

        return AddressSearchResponse.builder()
                .count(addresses.size())
                .results(addresses.stream()
                        .map(this::toResponse)
                        .toList())
                .build();
    }
}