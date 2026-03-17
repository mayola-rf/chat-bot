package com.playground.chatbot.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/rag")
public class RAGController {

    private ChatClient chatClient;
    OllamaChatModel ollamaChatModel;
    private VectorStore vectorStore;

    RAGController(OllamaChatModel ollamaChatModel, OllamaEmbeddingModel ollamaEmbeddingModel) {
        this.ollamaChatModel = ollamaChatModel;
        chatClient = ChatClient.builder(ollamaChatModel).build();
        vectorStore = SimpleVectorStore.builder(ollamaEmbeddingModel).build();
    }

    private void seedVectorStore() {
        vectorStore.add(List.of(Document.builder()
                        .text("Customer data should not be stored for more than 30 days")
                        // country == 'UK' && year >= 2020 && isActive == true
                        .metadata(Map.of("country", "UK"))
                        .build(),
                Document.builder()
                        .text("Customer data should not be stored for more than 78 days")
                        // country == 'UK' && year >= 2020 && isActive == true
                        .metadata(Map.of("country", "India"))
                        .build(),
                Document.builder()
                        .text("refund allowed within 30 days")
                        .metadata(Map.of("country", "India"))
                        .build()));
    }

    @GetMapping
    public String generateRAGResponse(String text) {
        seedVectorStore();
        return this.chatClient.prompt(text)
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .user(text)
                .call().content();
    }

    @GetMapping("/filters")
    public String generateRAGResponseWithFilter(String text) {
        seedVectorStore();
        ChatClient chatClient1 = ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
//                        .searchRequest(SearchRequest.builder().similarityThreshold(0.8d).topK(5)
//                                .filterExpression("country == 'India'")
//                                .build())
                        .build())
                .build();
        return chatClient1
                .prompt(text)
                .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "country == 'India'"))
//                .user(text)
                .call()
                .content();
    }
}
