package com.projects.kozip.address.repository;

import com.projects.kozip.address.entity.RoadAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoadAddressRepository extends JpaRepository<RoadAddress, String> {
    boolean existsBySidoContaining(String sido);
    boolean existsBySigunguContaining(String sigungu);

    @Query("SELECT r FROM RoadAddress r WHERE (" +
            "r.sigungu LIKE %:keyword% OR r.eupmyeondong LIKE %:keyword% OR " +
            "r.ri LIKE %:keyword% OR r.roadName LIKE %:keyword% OR " +
            "r.dong LIKE %:keyword% OR r.buildingName LIKE %:keyword%) " +
            "AND (:buildingMain IS NULL OR r.buildingMain = :buildingMain)")
    List<RoadAddress> search(@Param("keyword") String keyword, @Param("buildingMain") Integer buildingMain);

    @Query("SELECT COUNT(r) FROM RoadAddress r WHERE (" +
            "r.sigungu LIKE %:keyword% OR r.eupmyeondong LIKE %:keyword% OR " +
            "r.ri LIKE %:keyword% OR r.roadName LIKE %:keyword% OR " +
            "r.dong LIKE %:keyword% OR r.buildingName LIKE %:keyword%) " +
            "AND (:buildingMain IS NULL OR r.buildingMain = :buildingMain)")
    long countSearch(@Param("keyword") String keyword, @Param("buildingMain") Integer buildingMain);
}