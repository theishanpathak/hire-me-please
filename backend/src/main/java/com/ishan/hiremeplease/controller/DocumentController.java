package com.ishan.hiremeplease.controller;

import com.ishan.hiremeplease.dto.ChunkData;
import com.ishan.hiremeplease.dto.JDAnalysisResult;
import com.ishan.hiremeplease.dto.JDRequest;
import com.ishan.hiremeplease.entity.DocumentChunk;
import com.ishan.hiremeplease.service.DocumentProcessingService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("api/document")
@CrossOrigin(origins = "http://localhost:5173/")
public class DocumentController {
    private final DocumentProcessingService processingService;

    public DocumentController(DocumentProcessingService processingService) {
        this.processingService = processingService;
    }

   @PostMapping
    public Mono<String> uploadJD(@RequestBody JDRequest req){
        return processingService.processDocument(req.docuId(), req.documentText());
    }

    @GetMapping("/{jobId}")
    public JDAnalysisResult getJDAnalysis(@PathVariable String jobId){
        List<DocumentChunk> chunks = processingService.getById(jobId);

        List<ChunkData> chunkDataList = chunks.stream()
                .map(chunk -> new ChunkData(
                        //accesing he jsonB map safely
                        chunk.getMetadata().getOrDefault("section", "Unkown").toString(),
                        chunk.getChunkText(),
                        (Integer) chunk.getMetadata().getOrDefault("line", 0)
                        ))
                .toList();

        return new JDAnalysisResult(
                jobId,
                chunks.size(),
                chunkDataList
        );
    }

}
