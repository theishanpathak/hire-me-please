package com.ishan.hiremeplease.dto.matching;

public record MatchSummary(
        int totalRequirements,
        int strongMatches,
        int partialMatches,
        int gaps
) {
}
