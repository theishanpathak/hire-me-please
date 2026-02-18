package com.ishan.hiremeplease.dto.embedding;

public record EmbeddingRequest(
        String model,
        String prompt
) {
}
