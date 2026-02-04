package com.ishan.hiremeplease.controller;

import com.ishan.hiremeplease.entity.DocumentChunk;
import com.ishan.hiremeplease.service.DocumentProcessingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test")
public class Test {
    private final DocumentProcessingService processingService;

    public Test(DocumentProcessingService processingService) {
        this.processingService = processingService;
    }

    @GetMapping
    public List<DocumentChunk> getTest(){
        return processingService.getById("123");
    }

    @PostMapping
    public String postTest (@RequestBody String documentText){
        return processingService.processDocument("123", documentText).block();
    }
}
