package com.projects.kozip.address.controller;

import com.projects.kozip.address.dto.AddressSearchResponse;
import com.projects.kozip.address.service.RoadAddressService;
import com.projects.kozip.common.response.BaseResponse;
import com.projects.kozip.common.response.ResponseCode;
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
    public ResponseEntity<BaseResponse<AddressSearchResponse>> search(@RequestParam String query) {

        long start = System.nanoTime();

        AddressSearchResponse response = service.search(query);

        ResponseCode code = response.count() == 0
                ? ResponseCode.ADDRESS_SEARCH_EMPTY
                : ResponseCode.ADDRESS_SEARCH_OK;

        log.info("주소 검색 완료: code={}, count={}, elapsedMs={}",
                code.name(),
                response.count(),
                (System.nanoTime() - start) / 1_000_000);

        return ResponseEntity.ok(BaseResponse.success(code, response));
    }
}
