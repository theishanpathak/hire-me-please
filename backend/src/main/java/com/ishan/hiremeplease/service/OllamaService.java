package com.ishan.hiremeplease.service;

import com.ishan.hiremeplease.dto.embedding.EmbeddingRequest;
import com.ishan.hiremeplease.dto.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class OllamaService {
    private final WebClient webClient;
    private static final String EMBEDDING_MODEL = "nomic-embed-text";


    public OllamaService(@Qualifier("ollamaClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public  Mono<float[]> generateEmbedding(String text){
        EmbeddingRequest request = new EmbeddingRequest(EMBEDDING_MODEL, text);

        return webClient.post()
                .uri("/embeddings")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .map(EmbeddingResponse::embedding)
                .onErrorMap(e -> new RuntimeException("Ollama Embedding failed "+ e.getMessage()));
    }

}
