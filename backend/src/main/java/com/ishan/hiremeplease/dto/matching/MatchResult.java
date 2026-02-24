package com.ishan.hiremeplease.dto.matching;

import java.util.List;

public record MatchResult(
        String resumeId,
        String jdId,
        double overallScore,
        List<RequirementMatch> allMatches,
        List<SkillGap> gaps,
        MatchSummary summary

) {
}
