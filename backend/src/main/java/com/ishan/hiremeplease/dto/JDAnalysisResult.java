package com.ishan.hiremeplease.dto;

import java.util.List;

public record JDAnalysisResult(
        String documentId,
        int totalChunks,
        List<ChunkData> chunkData
) {
}
