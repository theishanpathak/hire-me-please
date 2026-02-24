package com.ishan.hiremeplease.dto.matching;


import java.util.List;

public record RequirementMatch(
    String jdRequirement,
    String jdSection,
    List<ResumeChunkMatch> resumeMatches,
    MatchStatus status,
    double bestSimilarity
) {
}
