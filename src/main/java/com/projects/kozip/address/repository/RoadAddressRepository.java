package com.projects.kozip.address.repository;

import com.projects.kozip.address.entity.RoadAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoadAddressRepository extends JpaRepository<RoadAddress, String> {
    boolean existsBySigungu(String sigungu);

    List<RoadAddress> findByZipCode(String zipCode);

    @Query(value = "SELECT * FROM road_address WHERE " +
            "MATCH(sigungu, eupmyeondong, ri, road_name, dong, building_name) " +
            "AGAINST(:keyword IN BOOLEAN MODE) " +
            "AND (:sido IS NULL OR sido = :sido) " +
            "AND (:buildingMain IS NULL OR building_main = :buildingMain) " +
            "LIMIT 1001",
            nativeQuery = true)
    List<RoadAddress> search(
            @Param("keyword") String keyword,
            @Param("buildingMain") Integer buildingMain,
            @Param("sido") String sido);
}