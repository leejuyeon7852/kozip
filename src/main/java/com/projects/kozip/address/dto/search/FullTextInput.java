package com.projects.kozip.address.dto.search;

public record FullTextInput(
        String keyword,
        String matchKeyword,
        Integer buildingMain) {

    public boolean isTooShort() {
        return keyword.length() < 2;
    }
}