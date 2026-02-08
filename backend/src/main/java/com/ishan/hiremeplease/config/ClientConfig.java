package com.ishan.hiremeplease.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {

    @Bean
    @Qualifier("ollamaClient")
    public WebClient ollamaWebClient(){
        return WebClient.builder()
                .baseUrl("http://localhost:11434/api")
                .build();
    }

    @Bean
    @Qualifier("llamaParseClient")
    public WebClient llamaParseWebClient(){
        return WebClient.builder()
                .baseUrl("https://api.cloud.llamaindex.ai")
                .build();
    }
}
