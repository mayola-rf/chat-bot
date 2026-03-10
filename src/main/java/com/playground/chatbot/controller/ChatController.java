package com.playground.chatbot.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ChatController {
    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ai")
    String generation(String userInput) {
        return this.chatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }

    @GetMapping("/ai/weather")
    String weather(String country, String state) {
        return this.chatClient.prompt()
                .system(s -> s.text("Let's check the whether across {country}")
                        .param("country", country)
                        .metadata("version", "1.0")
                        .metadata("model", "gpt-4"))
                .user(u -> u.text("How's the weather like in {state}")
                        .param("state", state))
                .call()
                .content();
    }

    @GetMapping("/ai/prompt")
    String prompt(String country, String state) {
        PromptTemplate promptTemplate = new PromptTemplate("How's the weather like in {state}");
        Prompt userPrompt = promptTemplate.create(Map.of("state", state));


        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("You are an AI Assistant who knows weather across {country}");
        Prompt systemPrompt = systemPromptTemplate.create(Map.of("country", country));

        Prompt prompt = new Prompt(List.of(userPrompt.getUserMessage(), systemPrompt.getSystemMessage()));
        return this.chatClient.prompt(prompt)
                .call()
                .content();
    }
}
