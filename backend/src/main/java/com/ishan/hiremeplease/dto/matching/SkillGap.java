package com.ishan.hiremeplease.dto.matching;

public record SkillGap(
        String requirement,
        String jdSection,
        double bestSimilarity,
        String closestResumeMatch
) {
}
