package com.projects.kozip.address.service;

import com.projects.kozip.address.dto.AddressResponse;
import com.projects.kozip.address.dto.AddressSearchResponse;
import com.projects.kozip.address.entity.RoadAddress;
import com.projects.kozip.address.repository.RoadAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoadAddressService {
    private final RoadAddressRepository repository;

    private static final int TOO_MANY_RESULTS = 1000;

    private static String normalizeSido(String token) {
        return switch (token) {
            case "서울", "서울시", "서울특별시" -> "서울특별시";
            case "부산", "부산시", "부산광역시" -> "부산광역시";
            case "대구", "대구시", "대구광역시" -> "대구광역시";
            case "인천", "인천시", "인천광역시" -> "인천광역시";
            case "광주", "광주시", "광주광역시" -> "광주광역시";
            case "대전", "대전시", "대전광역시" -> "대전광역시";
            case "울산", "울산시", "울산광역시" -> "울산광역시";
            case "세종", "세종시", "세종특별자치시" -> "세종특별자치시";
            case "경기", "경기도" -> "경기도";
            case "강원", "강원도", "강원특별자치도" -> "강원특별자치도";
            case "충북", "충청북도" -> "충청북도";
            case "충남", "충청남도" -> "충청남도";
            case "전북", "전라북도", "전북특별자치도" -> "전북특별자치도";
            case "전남", "전라남도" -> "전라남도";
            case "경북", "경상북도" -> "경상북도";
            case "경남", "경상남도" -> "경상남도";
            case "제주", "제주도", "제주특별자치도" -> "제주특별자치도";
            default -> null;
        };
    }

    public AddressSearchResponse search(String query) {
        String trimmed = query.trim();

        if (trimmed.length() < 2) {
            return AddressSearchResponse.builder()
                    .message("검색어는 2글자 이상 입력해주세요.")
                    .count(0)
                    .results(List.of())
                    .build();
        }
        // 우편번호 조회
        if (trimmed.matches("\\d{5}")) {
            return searchByZipCode(trimmed);
        }

        String[] parts = trimmed.split("\\s+");
        String sido = normalizeSido(parts[0]);
        if (sido != null) {
            parts = Arrays.copyOfRange(parts, 1, parts.length);
        }
        String remaining = String.join(" ", parts);
        // 시도만 입력하거나, 남은 검색어가 시군구뿐이면 상세 입력 요청
        long regionStart = System.nanoTime();

        boolean regionCandidate = remaining.matches("[가-힣]+[시군구]");

        boolean regionOnly = parts.length == 0
                || (regionCandidate && repository.existsBySigungu(remaining));

        log.info("검색 단계: 지역 검사, 소요시간: {}ms",
                (System.nanoTime() - regionStart) / 1_000_000);

        if (regionOnly) {
            return AddressSearchResponse.builder()
                    .message("더 자세하게 검색해주세요.")
                    .count(0)
                    .results(List.of())
                    .build();
        }

        Integer buildingMain = null;
        String[] keywordParts = parts;

        if (parts.length > 1 && parts[parts.length - 1].matches("\\d+")) {
            buildingMain = Integer.parseInt(parts[parts.length - 1]);
            keywordParts = Arrays.copyOf(parts, parts.length - 1);
        }

        String keyword = String.join("", keywordParts);

        if (keyword.length() < 2) {
            return AddressSearchResponse.builder()
                    .message("검색어는 2글자 이상 입력해주세요.")
                    .count(0)
                    .results(List.of())
                    .build();
        }

        String matchKeyword = Arrays.stream(keywordParts)
                .map(p -> "+" + p + "*")
                .collect(java.util.stream.Collectors.joining(" "));

        long searchStart = System.nanoTime();

        List<RoadAddress> addresses =
                repository.search(matchKeyword, buildingMain, sido);

        log.info("검색 단계: FULLTEXT 결과 조회, 결과: {}, 소요시간: {}ms",
                addresses.size(), (System.nanoTime() - searchStart) / 1_000_000);

        if (addresses.size() > TOO_MANY_RESULTS) {
            return AddressSearchResponse.builder()
                    .message("검색 결과가 너무 많습니다. 더 자세하게 검색해주세요.")
                    .count(addresses.size())
                    .results(List.of())
                    .build();
        }

        List<AddressResponse> results = addresses.stream()
                .map(this::toResponse)
                .toList();

        return AddressSearchResponse.builder()
                .count(results.size())
                .results(results)
                .build();
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
}