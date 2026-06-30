package com.projects.kozip.address.controller;

import com.projects.kozip.address.dto.AddressSearchResponse;
import com.projects.kozip.address.service.RoadAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
@Tag(name="주소 검색")
public class RoadAddressController {
    private final RoadAddressService service;

    @GetMapping
    @Operation(summary = "주소 검색 API")
    public ResponseEntity<AddressSearchResponse> search(@RequestParam String query){
        return ResponseEntity.ok(service.search(query));
    }
}
