package com.ishan.hiremeplease.dto.matching;

public record ResumeChunkMatch(
        String resumeText,
        String resumeSection,
        double similarity
) {
}
