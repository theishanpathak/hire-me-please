package com.ishan.hiremeplease.service;

import com.ishan.hiremeplease.dto.ChunkData;
import com.ishan.hiremeplease.entity.DocumentChunk;
import com.ishan.hiremeplease.repository.DocumentChunkRepository;
import com.ishan.hiremeplease.util.DocumentChunker;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.print.Doc;
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



    //this main service handles the reactive AI part
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
                .publishOn(Schedulers.boundedElastic()) //moving to a different thread to keep it working
                .map(documentChunks -> {
                    //transactional work for database action
                    return saveAllAtOnce(documentChunks, docuId);
                 })
                .onErrorResume(e -> Mono.just("Failed to process: " + e.getMessage()));

    }

    //All saved or nothing saved
    @Transactional
    public String saveAllAtOnce(List<DocumentChunk> chunks, String docuId){
        documentChunkRepository.saveAll(chunks);
        return "Succesfully saved " + chunks.size() + " of the document " + docuId;
    }

    public List<DocumentChunk> getById(String docuId){
        return documentChunkRepository.findByDocumentId(docuId);
    }
}
