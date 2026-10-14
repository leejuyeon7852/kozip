package com.projects.kozip.address.mapper;

import com.projects.kozip.address.dto.AddressResponse;
import com.projects.kozip.address.entity.RoadAddress;
import org.springframework.stereotype.Component;

@Component
public class AddressResponseMapper {

    public AddressResponse toResponse(RoadAddress address) {
        String building = address.getBuildingMain()
                + (address.getBuildingSub() != 0
                ? "-" + address.getBuildingSub() : "");

        String jibun = address.getJibunMain()
                + (address.getJibunSub() != 0
                ? "-" + address.getJibunSub() : "");

        String roadAddress = String.join(" ",
                address.getSido(),
                address.getSigungu() != null
                        ? address.getSigungu() : "",
                address.getEupmyeondong(),
                address.getRoadName(),
                building
        ).replaceAll(" +", " ").trim();

        if (address.getBuildingName() != null) {
            roadAddress += " (" + address.getBuildingName() + ")";
        }

        String jibunAddress = String.join(" ",
                address.getSido(),
                address.getSigungu() != null
                        ? address.getSigungu() : "",
                address.getEupmyeondong(),
                address.getRi() != null
                        ? address.getRi() : "",
                jibun
        ).replaceAll(" +", " ").trim();

        return AddressResponse.builder()
                .zipCode(address.getZipCode())
                .roadAddress(roadAddress)
                .jibunAddress(jibunAddress)
                .build();
    }
}