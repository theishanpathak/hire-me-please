package com.ishan.hiremeplease.service;

import com.ishan.hiremeplease.entity.DocumentChunk;
import com.ishan.hiremeplease.repository.DocumentChunkRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentRepoService {
    private final DocumentChunkRepository repository;

    public DocumentRepoService(DocumentChunkRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void saveAllAtOnce(List<DocumentChunk> chunks, String docuId){
        repository.saveAll(chunks);
        System.out.println("Succesfully saved " + chunks.size() + " of the document " + docuId);
    }

    public List<DocumentChunk> getById(String docuId){
        return repository.findByDocumentId(docuId);
    }
}
