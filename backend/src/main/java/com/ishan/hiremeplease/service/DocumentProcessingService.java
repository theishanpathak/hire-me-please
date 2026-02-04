package com.ishan.hiremeplease.service;

import com.ishan.hiremeplease.dto.ChunkData;
import com.ishan.hiremeplease.entity.DocumentChunk;
import com.ishan.hiremeplease.repository.DocumentChunkRepository;
import com.ishan.hiremeplease.util.DocumentChunker;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocumentProcessingService {
    private final OllamaService ollamaService;
    private final DocumentChunker documentChunker;
    private final DocumentChunkRepository documentChunkRepository;

    public DocumentProcessingService(OllamaService ollamaService, DocumentChunker documentChunker, DocumentChunkRepository documentChunkRepository) {
        this.ollamaService = ollamaService;
        this.documentChunker = documentChunker;
        this.documentChunkRepository = documentChunkRepository;
    }

    
    public Mono<String> processDocument(String docuId, String documentText){
        //chunk the document using docuChunker:: this will give us List of chunked data
        List<ChunkData> chunks = documentChunker.chunkByLines(documentText);

        if (chunks.isEmpty()) {
            return Mono.just("No chunks found in document");
        }

        //take the chunk text from the chunked data and pass it into ollama service to get chunked
        //this returns us embeddings
        return Flux.fromIterable(chunks)
                .flatMapSequential(chunkData -> ollamaService.generateEmbedding(chunkData.chunk())
                        .map(embedding -> new DocumentChunk(
                                docuId,
                                chunkData.chunk(),
                                embedding,
                                Map.of("section", chunkData.sectionName(), "line", chunkData.startLine())
                        ))
                )
                .collectList()
                .map(documentChunks -> {
                    List<DocumentChunk> saved = documentChunkRepository.saveAll(documentChunks);
                    return "Processed " + saved.size() + " chunks for document " + docuId;
                 })
                .onErrorResume(e -> Mono.just("Failed to process: " + e.getMessage()));

    }

    public List<DocumentChunk> getById(String docuId){
        return documentChunkRepository.findByDocumentId(docuId);
    }
}
