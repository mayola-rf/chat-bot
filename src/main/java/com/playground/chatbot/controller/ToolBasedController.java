package com.playground.chatbot.controller;

import com.playground.chatbot.tool.ChatBotTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ToolBasedController {

    private final ChatClient chatClientWithTool;

    public ToolBasedController(OllamaChatModel ollamaChatModel, ChatBotTools chatBotTools) {
        this.chatClientWithTool = ChatClient.builder(ollamaChatModel)
                .defaultTools(chatBotTools)
                .build();
    }

    @GetMapping("/ai/using-tools")
    public String usingTools(String request) {
        return this.chatClientWithTool.prompt()
                .system(s -> s.text("""
                        You are an assistant.
                        For any question about current date or time and any questions related to math calculations,
                        you MUST call the available tools.
                        Do not guess the answer yourself.
                        """))
                .user(u -> u.text(request))
                .call()
                .content();
    }
    @GetMapping("/ai/using-tools-callback")
    public String usingToolsCallback(String request) {
        return this.chatClientWithTool.prompt()
                .system(s -> s.text("""
                        You are an assistant.
                        For any question about current date or time and any questions related to math calculations,
                        you MUST call the available tools.
                        Do not guess the answer yourself.
                        """))
                .user(u -> u.text(request))
                .call()
                .content();
    }
}
