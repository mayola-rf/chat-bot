package com.playground.chatbot.controller;

import com.playground.chatbot.tool.ProductTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ai/product")
public class ProductController {

    private final ChatClient chatClientWithTool;

    public ProductController(OllamaChatModel ollamaChatModel, ProductTools productTools, ChatMemory chatMemory) {
        this.chatClientWithTool = ChatClient.builder(ollamaChatModel)
                .defaultTools(productTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @GetMapping("/recommend")
    public String recommend(@RequestParam String query) {
        return this.chatClientWithTool.prompt()
                .system("""
                        You are a product recommendation assistant. Laptop is a product.
                        You can find laptops, retrieve their specs, compare them and recommend the best one.
                        You have ProductTools at your disposal to achieve this
                        """)
                .user(query)
                .call()
                .content();
    }

    @GetMapping("/memory")
    public String recommend(@RequestParam int memoryId, @RequestParam String query) {
        return this.chatClientWithTool.prompt()
                .user(query)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, memoryId))
                .call()
                .content();
    }
}
