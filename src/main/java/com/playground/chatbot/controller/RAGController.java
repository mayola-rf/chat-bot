package com.playground.chatbot.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai/rag")
public class RAGController {

    private ChatClient chatClient;
    private VectorStore vectorStore;

    RAGController(OllamaChatModel ollamaChatModel, OllamaEmbeddingModel ollamaEmbeddingModel) {
        chatClient = ChatClient.builder(ollamaChatModel).build();
        vectorStore = SimpleVectorStore.builder(ollamaEmbeddingModel).build();
    }

    @GetMapping
    public String generateRAGResponse(String text) {
        vectorStore.add(List.of(new Document("Customer data should not be stored for more than 30 days")));
        return this.chatClient.prompt(text)
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .user(text)
                .call().content();
    }
}
