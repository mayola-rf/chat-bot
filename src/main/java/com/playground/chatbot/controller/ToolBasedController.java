package com.playground.chatbot.controller;

import com.playground.chatbot.tool.DateTimeTools;
import com.playground.chatbot.tool.MathTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ToolBasedController {

    private final ChatClient chatClientWithTool;

    public ToolBasedController(OllamaChatModel ollamaChatModel, DateTimeTools chatBotTools, MathTools mathTools) {
        this.chatClientWithTool = ChatClient.builder(ollamaChatModel)
                .defaultTools(chatBotTools, mathTools)
                .build();
    }

    @GetMapping("/ai/using-tools")
    public String usingTools(String request) {
        return this.chatClientWithTool.prompt()
                .system(s -> s.text("""
                        You are super assistant. Do not guess the answer yourself.
                        1. For any question about current date or time, you MUST call the available DateTimeTools.
                        2. For any question on math calculations, you MUST call the available MathTools.
                        """))
                .user(u -> u.text(request))
                .call()
                .content();
    }
}
