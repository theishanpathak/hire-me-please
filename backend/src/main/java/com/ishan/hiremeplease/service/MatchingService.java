package com.ishan.hiremeplease.service;

import com.ishan.hiremeplease.dto.matching.MatchResult;
import com.ishan.hiremeplease.dto.matching.MatchStatus;
import com.ishan.hiremeplease.dto.matching.RequirementMatch;
import com.ishan.hiremeplease.entity.DocumentChunk;
import com.ishan.hiremeplease.repository.DocumentChunkRepository;
import com.ishan.hiremeplease.util.VectorUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchingService {
    private final DocumentChunkRepository repository;
    private final VectorUtils vectorUtils;

    private static final double STRONG_THRESHOLD = 0.7;
    private static final double PARTIAL_THRESHOLD = 0.5;

    public MatchingService(DocumentChunkRepository repository, VectorUtils vectorUtils) {
        this.repository = repository;
        this.vectorUtils = vectorUtils;
    }

    public MatchResult analyzeMatch(String resumeId, String jdId) {
        // TODO: Get JD chunks
        List<DocumentChunk> chunks = repository.findByDocumentId(jdId);
        // TODO: Analyze each requirement
        // TODO: Calculate score
        // TODO: Build gaps
        // TODO: Return result

    }

    private RequirementMatch analyzeRequirement(DocumentChunk jdChunk, String resumeId) {
        // TODO: Convert embedding to string
        // TODO: Find similar resume chunks
        // TODO: Calculate similarities
        // TODO: Determine status
    }




    private MatchStatus determineStatus(double similarity){
        if(similarity >= STRONG_THRESHOLD) return MatchStatus.STRONG_MATCH;
        if(similarity >= PARTIAL_THRESHOLD) return MatchStatus.PARTIAL_MATCH;
        return MatchStatus.GAP;
    }
}
