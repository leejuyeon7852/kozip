package com.projects.kozip.address.controller;

import com.projects.kozip.address.dto.AddressSearchResponse;
import com.projects.kozip.address.service.RoadAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
@Tag(name="주소 검색")
@Slf4j
public class RoadAddressController {
    private final RoadAddressService service;

    @GetMapping
    @Operation(summary = "주소 검색 API")
    public ResponseEntity<AddressSearchResponse> search(@RequestParam String query){
        long start = System.currentTimeMillis();
        AddressSearchResponse response = service.search(query);
        long elapsed = System.currentTimeMillis() - start;
        log.info("검색어: {}, 결과: {}, 소요시간: {}ms", query, response.getCount(), elapsed);

        return ResponseEntity.ok(response);
    }
}
