package com.projects.kozip.address.service;

import com.projects.kozip.address.mapper.AddressResponseMapper;
import com.projects.kozip.address.repository.RoadAddressRepository;
import com.projects.kozip.address.search.AddressQueryParser;
import com.projects.kozip.common.exception.SearchException;
import com.projects.kozip.common.response.ResponseCode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RoadAddressServiceTest {
    private final RoadAddressRepository repository = mock(RoadAddressRepository.class);
    private final RoadAddressService service =
            new RoadAddressService(
                    repository,
                    new AddressQueryParser(),
                    new AddressResponseMapper());

    @Test
    void zipCodeSkipsRegionQueries() {
        service.search("06231");
        verify(repository).findByZipCode("06231");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void sidoOnlyRequestsMoreDetailWithoutQueryingDatabase() {
        SearchException exception = assertThrows(
                SearchException.class,
                () -> service.search("서울"));

        assertEquals(
                ResponseCode.SEARCH_QUERY_TOO_BROAD,
                exception.responseCode());

        verifyNoInteractions(repository);
    }

    @Test
    void sidoAndSigunguOnlyRequestMoreDetail() {
        when(repository.existsBySigungu("강남구")).thenReturn(true);

        SearchException exception = assertThrows(
                SearchException.class,
                () -> service.search("서울 강남구"));

        assertEquals(
                ResponseCode.SEARCH_QUERY_TOO_BROAD,
                exception.responseCode());

        verify(repository).existsBySigungu("강남구");
        verifyNoMoreInteractions(repository);
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

}
