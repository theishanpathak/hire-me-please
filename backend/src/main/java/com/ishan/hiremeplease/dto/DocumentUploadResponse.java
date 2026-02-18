package com.ishan.hiremeplease.dto;

public record DocumentUploadResponse(
        String documentId,
        String status,
        String message
) {
}
