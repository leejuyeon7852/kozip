package com.projects.kozip.address.controller;

import com.projects.kozip.address.dto.AddressSearchResponse;
import com.projects.kozip.address.service.RoadAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class RoadAddressController {
    private final RoadAddressService service;

    @GetMapping
    public ResponseEntity<AddressSearchResponse> search(@RequestPart String query){
        return ResponseEntity.ok(service.search(query));
    }
}
