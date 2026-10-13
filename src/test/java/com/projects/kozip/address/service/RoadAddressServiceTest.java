package com.projects.kozip.address.service;

import com.projects.kozip.address.repository.RoadAddressRepository;
import com.projects.kozip.address.search.AddressQueryParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RoadAddressServiceTest {
    private final RoadAddressRepository repository = mock(RoadAddressRepository.class);
    private final RoadAddressService service =
            new RoadAddressService(repository, new AddressQueryParser());

    @Test
    void zipCodeSkipsRegionQueries() {
        service.search("06231");
        verify(repository).findByZipCode("06231");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void sidoOnlyRequestsMoreDetailWithoutQueryingDatabase() {
        assertEquals("더 자세하게 검색해주세요.", service.search("서울").getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void fullAddressUsesRoadSearchWithRegionFilters() {
        service.search("서울 강남구 테헤란로33길");

        verify(repository).searchByRoad(
                "테헤란로33길",
                "서울특별시",
                "강남구",
                null,
                null);

        verifyNoMoreInteractions(repository);
    }

    @Test
    void sidoAndSigunguOnlyRequestMoreDetail() {
        when(repository.existsBySigungu("강남구")).thenReturn(true);
        assertEquals("더 자세하게 검색해주세요.", service.search("서울 강남구").getMessage());
        verify(repository).existsBySigungu("강남구");
        verifyNoMoreInteractions(repository);
    }
}
