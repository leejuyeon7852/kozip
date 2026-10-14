package com.projects.kozip.address.service;

import com.projects.kozip.address.dto.AddressResponse;
import com.projects.kozip.address.dto.AddressSearchResponse;
import com.projects.kozip.address.dto.search.FullTextInput;
import com.projects.kozip.address.dto.search.RegionInput;
import com.projects.kozip.address.dto.search.RoadInput;
import com.projects.kozip.address.entity.RoadAddress;
import com.projects.kozip.address.mapper.AddressResponseMapper;
import com.projects.kozip.address.repository.RoadAddressRepository;
import com.projects.kozip.address.search.AddressQueryParser;
import com.projects.kozip.common.exception.SearchException;
import com.projects.kozip.common.response.ResponseCode;
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
    private final AddressResponseMapper responseMapper;

    private static final int TOO_MANY_RESULTS = 1000;

    public AddressSearchResponse search(String query) {
        String trimmed = query.trim();

        if (trimmed.length() < 2) {
            throw new SearchException(ResponseCode.SEARCH_QUERY_TOO_SHORT);
        }

        if (queryParser.isZipCode(trimmed)) {
            return searchByZipCode(trimmed);
        }

        RegionInput region = queryParser.parseRegion(trimmed);

        long regionStart = System.nanoTime();

        boolean regionOnly = region.isSidoOnly()
                || (region.isSigunguCandidate()
                && repository.existsBySigungu(region.remaining()));

        log.debug("검색 단계: 지역 검사, 소요시간: {}ms",
                (System.nanoTime() - regionStart) / 1_000_000);

        if (regionOnly) {
            throw new SearchException(ResponseCode.SEARCH_QUERY_TOO_BROAD);
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

            log.debug("검색 단계: 도로명 조회, 결과: {}, 소요시간: {}ms",
                    addresses.size(),
                    (System.nanoTime() - roadStart) / 1_000_000);

            return toSearchResponse(addresses);
        }

        FullTextInput fullTextInput = queryParser.parseFullText(parts);

        if (fullTextInput.isTooShort()) {
            throw new SearchException(ResponseCode.SEARCH_QUERY_TOO_SHORT);
        }

        long searchStart = System.nanoTime();

        List<RoadAddress> addresses = repository.search(
                fullTextInput.matchKeyword(),
                fullTextInput.buildingMain(),
                region.sido());

        log.debug("검색 단계: FULLTEXT 결과 조회, 결과: {}, 소요시간: {}ms",
                addresses.size(),
                (System.nanoTime() - searchStart) / 1_000_000);

        return toSearchResponse(addresses);
    }

    private AddressSearchResponse searchByZipCode(String zipCode) {
        List<RoadAddress> addresses = repository.findByZipCode(zipCode);
        List<AddressResponse> results = addresses.stream()
                .map(responseMapper::toResponse)
                .toList();

        return AddressSearchResponse.builder()
                .count(results.size())
                .results(results)
                .build();
    }

    private AddressSearchResponse toSearchResponse(
            List<RoadAddress> addresses) {

        if (addresses.size() > TOO_MANY_RESULTS) {
            throw new SearchException(ResponseCode.SEARCH_TOO_MANY_RESULTS);
        }

        return AddressSearchResponse.builder()
                .count(addresses.size())
                .results(addresses.stream()
                        .map(responseMapper::toResponse)
                        .toList())
                .build();
    }
}