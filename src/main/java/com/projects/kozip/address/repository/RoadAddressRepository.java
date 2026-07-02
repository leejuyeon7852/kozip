package com.projects.kozip.address.repository;

import com.projects.kozip.address.entity.RoadAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoadAddressRepository extends JpaRepository<RoadAddress, String> {
    boolean existsBySidoContaining(String sido);
    boolean existsBySigunguContaining(String sigungu);

    List<RoadAddress> findByZipCode(String zipCode);

    @Query(value = "SELECT * FROM road_address WHERE " +
            "MATCH(sigungu, eupmyeondong, ri, road_name, dong, building_name) " +
            "AGAINST(:keyword IN BOOLEAN MODE) " +
            "AND (:buildingMain IS NULL OR building_main = :buildingMain)",
            nativeQuery = true)
    List<RoadAddress> search(@Param("keyword") String keyword, @Param("buildingMain") Integer buildingMain);

    @Query(value = "SELECT COUNT(*) FROM road_address WHERE " +
            "MATCH(sigungu, eupmyeondong, ri, road_name, dong, building_name) " +
            "AGAINST(:keyword IN BOOLEAN MODE) " +
            "AND (:buildingMain IS NULL OR building_main = :buildingMain)",
            nativeQuery = true)
    long countSearch(@Param("keyword") String keyword, @Param("buildingMain") Integer buildingMain);
}