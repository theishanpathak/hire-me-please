package com.ishan.hiremeplease.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class LlamaParseService {
    private final WebClient webClient;
    @Value("${llama.cloud.api.key}")
    private String apiKey;

    public LlamaParseService(@Qualifier("llamaParseClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public String parseResume(byte[] fileBytes, String fileName){

        //build the Multipart Request
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", fileBytes, MediaType.APPLICATION_PDF).filename(fileName);

        // v2 requires a JSON configuration part
        Map<String, Object> config =Map.of(
                "tier", "cost_effective",
                "version", "latest",
                "agentic_options", Map.of("custom_prompt", "This is a resume. Preserve columns."));

        builder.part("configuration", config, MediaType.APPLICATION_JSON);

        //v2 has upload and parse trigger together
        Map<String, Object> uploadResponse = webClient.post()
                .uri("/api/v2/parse/upload")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            System.err.println("LlamaParse rejected us: " + body);
                            return Mono.error(new RuntimeException("API Error: " + body));
                        })
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        if(uploadResponse == null || !uploadResponse.containsKey("id")){
            throw new RuntimeException("Failed to initiate LlamaParse Job");
        }

        String jobId = (String) uploadResponse.get("id");
        int attempts = 0;

        //poll for success
        while(attempts < 30){ //max wait time is of 1.25 minutes
            try {
                Thread.sleep(2500);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Polling interrupted", e);
            }
            Map<String, Object> resultResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v2/parse/{jobId}")
                            .queryParam("expand", "markdown")
                            .build(jobId))
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (resultResponse == null) {
                attempts++;
                continue;
            }

            Map<String, Object> jobData = (Map<String, Object>) resultResponse.get("job");
            String status = String.valueOf(jobData.get("status"));




            if("COMPLETED".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {

                // Navigate nested result: markdown -> pages -> [list of page markdown]

                Map<String, Object> markdownData = (Map<String, Object>) resultResponse.get("markdown");
                List<Map<String, Object>> pages = (List<Map<String, Object>>) markdownData.get("pages");
                StringBuilder finalMarkdown = new StringBuilder();
                for (Map<String, Object> page : pages) {
                    finalMarkdown.append(page.get("markdown")).append("\n\n");
                }
                return finalMarkdown.toString().trim();

            } else if("FAILED".equalsIgnoreCase(status) || "ERROR".equalsIgnoreCase(status)){
                String error = String.valueOf(jobData.get("error_message"));
                throw new RuntimeException("LlamaParse job failed: " + error);
            }
            attempts++;
        }
        throw new RuntimeException("LlamaParse job timed out after 30 attempts.");
    }
}
