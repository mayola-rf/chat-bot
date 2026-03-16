package com.playground.chatbot.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai/embeddings")
public class EmbeddingController {

    @Autowired
    OllamaEmbeddingModel ollamaEmbeddingModel;

    @GetMapping
    float[] generateEmbeddings() {
        EmbeddingRequest embeddingRequest = new EmbeddingRequest(
                List.of("Customer documents can be stored for not more than 3 months"), null);

        return ollamaEmbeddingModel.call(embeddingRequest)
                .getResult().getOutput();
    }
}
