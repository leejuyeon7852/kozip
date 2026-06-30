package com.projects.kozip.address.repository;

import com.projects.kozip.address.entity.RoadAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoadAddressRepository extends JpaRepository<RoadAddress, String> {
    boolean existsBySido(String sido);
    boolean existsBySigungu(String sigungu);

    @Query("SELECT r FROM RoadAddress r WHERE " +
            "r.sigungu LIKE %:query% OR " +
            "r.eupmyeondong LIKE %:query% OR " +
            "r.ri LIKE %:query% OR " +
            "r.roadName LIKE %:query% OR " +
            "r.dong LIKE %:query% OR " +
            "r.buildingName LIKE %:query%")
    List<RoadAddress> search(@Param("query") String query);
}
