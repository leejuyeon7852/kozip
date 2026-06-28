package com.projects.kozip.address.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name="road_address")
public class RoadAddress {
    @Id
    @Column(length = 26, nullable = false)
    private String manageNo; // 도로명주소관리번호

    @Column(length = 40, nullable = false)
    private String sido; // 시도명

    @Column(length = 40)
    private String sigungu; //시군구명

    @Column(length = 40, nullable = false)
    private String eupmyeondong; //법정읍면동

    @Column(length = 40)
    private String ri; //법정리명

    @Column(nullable = false)
    private int jibunMain; // 지번본번

    @Column(nullable = false)
    private int jibunSub; // 지번 부번

    @Column(length = 80, nullable = false)
    private String roadName; // 도로명

    @Column(nullable = false)
    private int buildingMain; //건물본번

    @Column(nullable = false)
    private int buildingSub; //건물부번

    @Column(length = 60)
    private String dong; //행정동면

    @Column(length = 5, nullable = false)
    private String zipCode; //우편번호

    @Column(length = 400)
    private String oldRoadAddress; //이전 도로명 주소

    @Column(length = 1, nullable = false)
    private String isApartment; //공동주택구분

    @Column(length = 400)
    private String buildingName; // 건물명
}
