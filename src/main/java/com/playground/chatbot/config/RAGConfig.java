package com.playground.chatbot.config;

import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RAGConfig {

//    @Bean // pgvector is added as the vector db now
//    VectorStore simpleVectorStore(final OllamaEmbeddingModel ollamaEmbeddingModel) {
//        return SimpleVectorStore.builder(ollamaEmbeddingModel).build();
//    }

    @Bean
    TokenTextSplitter tokenTextSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(150)
                .build();
    }
}
