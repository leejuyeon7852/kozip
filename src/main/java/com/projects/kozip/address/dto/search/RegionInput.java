package com.projects.kozip.address.dto.search;

public record RegionInput(
        String sido,
        String remaining) {

    public boolean isSidoOnly() {
        return remaining.isEmpty();
    }

    public boolean isSigunguCandidate() {
        return remaining.matches("[가-힣]+[시군구]");
    }

    public String[] parts() {
        return remaining.isEmpty()
                ? new String[0]
                : remaining.split("\\s+");
    }
}