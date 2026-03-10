package com.playground.chatbot.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
