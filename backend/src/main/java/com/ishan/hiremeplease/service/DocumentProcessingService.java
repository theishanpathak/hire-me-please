package com.ishan.hiremeplease.service;

import com.ishan.hiremeplease.dto.ChunkData;
import com.ishan.hiremeplease.entity.DocumentChunk;
import com.ishan.hiremeplease.util.DocumentChunker;
import com.ishan.hiremeplease.util.MarkdownChunker;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;


@Service
public class DocumentProcessingService {
    private final OllamaService ollamaService;
    private final DocumentChunker jDChunker;
    private final MarkdownChunker resumeChunker;
    private final DocumentRepoService repoService;


    public DocumentProcessingService(OllamaService ollamaService, DocumentChunker jDChunker, MarkdownChunker resumeChunker, DocumentRepoService repoService) {
        this.ollamaService = ollamaService;
        this.jDChunker = jDChunker;
        this.resumeChunker = resumeChunker;
        this.repoService = repoService;
    }

    public Mono<String> processResume(String docuId, String markdownText){
        if (markdownText == null || markdownText.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Resume content is blank"));
        }

        List<ChunkData> chunks = resumeChunker.chunkMarkdown(markdownText);
        return saveChunks(docuId, chunks);
    }

    public Mono<String> processJD(String docuId, String documentText){
        if (documentText == null || documentText.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Job description is blank"));
        }

        List<ChunkData> chunks = jDChunker.chunkByLines(documentText);
        return saveChunks(docuId, chunks);
    }

    public Mono<String> saveChunks(String docuId, List<ChunkData> chunks){
        if (chunks.isEmpty()) {
            return Mono.error(new RuntimeException("No content found in the document"));
        }

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
                .publishOn(Schedulers.boundedElastic()) //moving to a different thread to keep it working
                .map(documentChunks -> {
                    //transactional work for database action
                    repoService.saveAllAtOnce(documentChunks, docuId);
                    return "Successfully processed and saved the document";

                });
    }

    public List<DocumentChunk> getById(String docuId){
        return repoService.getById(docuId);
    }
}
