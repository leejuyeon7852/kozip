package com.projects.kozip.address.repository;

import com.projects.kozip.address.entity.RoadAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadAddressRepository extends JpaRepository<RoadAddress, String> {
}
