package com.projects.kozip.address.search;

import com.projects.kozip.address.dto.search.FullTextInput;
import com.projects.kozip.address.dto.search.RegionInput;
import com.projects.kozip.address.dto.search.RoadInput;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class AddressQueryParser {

    private static final Pattern BUILDING_NUMBER = Pattern.compile("^(\\d{1,6})(?:-(\\d{1,6}))?$");

    private static final Pattern ROAD_NAME = Pattern.compile("[가-힣A-Za-z0-9·.]+(?:대로|로|길)");

    public boolean isZipCode(String query) {
        return query.matches("\\d{5}");
    }

    public RegionInput parseRegion(String query) {
        String[] parts = query.split("\\s+");
        String sido = normalizeSido(parts[0]);

        if (sido != null) {
            parts = Arrays.copyOfRange(parts, 1, parts.length);
        }

        return new RegionInput(sido, String.join(" ", parts));
    }

    public RoadInput parseRoad(String[] parts) {
        int end = parts.length;
        Integer main = null;
        Integer sub = null;

        if (end > 1) {
            Matcher number = BUILDING_NUMBER.matcher(parts[end - 1]);

            if (number.matches()) {
                main = Integer.valueOf(number.group(1));
                sub = number.group(2) == null
                        ? 0 : Integer.valueOf(number.group(2));
                end--;
            }
        }

        int start = 0;
        String sigungu = null;

        if (end > 1 && parts[0].matches("[가-힣]+[시군구]")) {
            sigungu = parts[0];
            start++;
        }

        String roadName = String.join("",
                Arrays.copyOfRange(parts, start, end));

        if (!ROAD_NAME.matcher(roadName).matches()) {
            return null;
        }

        return new RoadInput(roadName, sigungu, main, sub);
    }

    public FullTextInput parseFullText(String[] parts) {
        Integer buildingMain = null;
        String[] keywordParts = parts;

        if (parts.length > 1
                && parts[parts.length - 1].matches("\\d+")) {
            buildingMain = Integer.parseInt(parts[parts.length - 1]);
            keywordParts = Arrays.copyOf(parts, parts.length - 1);
        }

        String keyword = String.join("", keywordParts);
        String matchKeyword = Arrays.stream(keywordParts)
                .map(part -> "+" + part + "*")
                .collect(Collectors.joining(" "));

        return new FullTextInput(keyword, matchKeyword, buildingMain);
    }

    private String normalizeSido(String token) {
        return switch (token) {
            case "서울", "서울시", "서울특별시" -> "서울특별시";
            case "부산", "부산시", "부산광역시" -> "부산광역시";
            case "대구", "대구시", "대구광역시" -> "대구광역시";
            case "인천", "인천시", "인천광역시" -> "인천광역시";
            case "광주", "광주시", "광주광역시" -> "광주광역시";
            case "대전", "대전시", "대전광역시" -> "대전광역시";
            case "울산", "울산시", "울산광역시" -> "울산광역시";
            case "세종", "세종시", "세종특별자치시" -> "세종특별자치시";
            case "경기", "경기도" -> "경기도";
            case "강원", "강원도", "강원특별자치도" -> "강원특별자치도";
            case "충북", "충청북도" -> "충청북도";
            case "충남", "충청남도" -> "충청남도";
            case "전북", "전라북도", "전북특별자치도" -> "전북특별자치도";
            case "전남", "전라남도" -> "전라남도";
            case "경북", "경상북도" -> "경상북도";
            case "경남", "경상남도" -> "경상남도";
            case "제주", "제주도", "제주특별자치도" -> "제주특별자치도";
            default -> null;
        };
    }
}