package com.ishan.hiremeplease.dto;

public record EmbeddingRequest(
        String model,
        String prompt
) {
}
