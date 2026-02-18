package com.ishan.hiremeplease.controller;

import com.ishan.hiremeplease.dto.ChunkData;
import com.ishan.hiremeplease.dto.DocumentUploadResponse;
import com.ishan.hiremeplease.dto.JDAnalysisResult;
import com.ishan.hiremeplease.dto.JDRequest;
import com.ishan.hiremeplease.entity.DocumentChunk;
import com.ishan.hiremeplease.service.DocumentProcessingService;
import com.ishan.hiremeplease.service.LlamaParseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController()
@RequestMapping("api/document")
@CrossOrigin(origins = "http://localhost:5173/")
public class DocumentController {
    private final DocumentProcessingService processingService;
    private final LlamaParseService parseService;

    public DocumentController(DocumentProcessingService processingService, LlamaParseService parseService) {
        this.processingService = processingService;
        this.parseService = parseService;
    }


   @PostMapping("/jd")
    public Mono<ResponseEntity<DocumentUploadResponse>> uploadJD(@RequestBody JDRequest req){
        String jdId = "jd-" + UUID.randomUUID();

        return processingService.processJD(jdId, req.documentText())
               .map(result -> ResponseEntity.ok(new DocumentUploadResponse(jdId, "success", result)))
               .onErrorResume(Throwable.class, e -> Mono.just(mapError(e)));
    }



    @PostMapping("/resume")
    public Mono<ResponseEntity<DocumentUploadResponse>> uploadResume(@RequestParam("file") MultipartFile file){
        String resumeId = "resume-" + UUID.randomUUID();

        return Mono.fromCallable(() -> file.getBytes())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(bytes -> {
                    String markdown = parseService.parseResume(bytes, file.getOriginalFilename());
                    return processingService.processResume(resumeId, markdown);
                })
                .map(success -> ResponseEntity.ok(new DocumentUploadResponse(resumeId, "success", "Resume processed")))
                .onErrorResume(Throwable.class, e -> Mono.just(mapError(e)));
    }


    //helper map error to centralize errors
    private ResponseEntity<DocumentUploadResponse> mapError(Throwable e) {
        if (e instanceof IllegalArgumentException) {
            return ResponseEntity.badRequest()
                    .body(new DocumentUploadResponse(null, "error", e.getMessage()));
        }
        return ResponseEntity.status(500)
                .body(new DocumentUploadResponse(null, "error", "Server Error: " + e.getMessage()));
    }


    @GetMapping("/{docuId}")
    public JDAnalysisResult getJDAnalysis(@PathVariable String docuId){
        List<DocumentChunk> chunks = processingService.getById(docuId);

        List<ChunkData> chunkDataList = chunks.stream()
                .map(chunk -> new ChunkData(
                        //accesing he jsonB map safely
                        chunk.getMetadata().getOrDefault("section", "Unknown").toString(),
                        chunk.getChunkText(),
                        (Integer) chunk.getMetadata().getOrDefault("line", 0)
                ))
                .toList();

        return new JDAnalysisResult(
                docuId,
                chunks.size(),
                chunkDataList
        );
    }


}




//    @PostMapping("/resume/upload")
//    public Mono<String> testParse(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("resumeId") String resumeId
//    ){
//        if(file.isEmpty()){
//            return Mono.error(new RuntimeException("No file uploaded"));
//        }
//
//        String markdown = parseService.parseResume(file.getBytes(), file.getOriginalFilename());
//        return processingService.processDocument(resumeId, markdown);
//    }


