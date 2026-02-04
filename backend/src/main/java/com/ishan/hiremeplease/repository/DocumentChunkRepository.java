package com.ishan.hiremeplease.repository;

import com.ishan.hiremeplease.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    //General similarity search for all documents
    @Query(value = "SELECT * FROM document_chunk " +
            "ORDER BY embedding <=> CAST(:queryEmbedding AS vector) LIMIT :topK", nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("topK") int topK);

    //Similarity search in the particular document
    @Query(value = "SELECT * FROM document_chunk WHERE document_id = :documentId " +
            "ORDER BY embedding <=> CAST(:queryEmbedding AS vector) LIMIT :topK", nativeQuery = true)
    List<DocumentChunk> findSimilarChunkByDocumentId(
            @Param("documentId") String documentId,
            @Param("queryEmbedding") String queryEmbedding,
            @Param("topK") int topK);

    List<DocumentChunk> findByDocumentId(String documentId);

}
