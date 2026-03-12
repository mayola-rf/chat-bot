package com.playground.chatbot.controller;

import com.playground.chatbot.tool.DateTimeTools;
import com.playground.chatbot.tool.MathTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class ToolBasedController {

    private final ChatClient chatClientWithTool;
    private final OllamaChatModel ollamaChatModel;

    public ToolBasedController(OllamaChatModel ollamaChatModel, DateTimeTools chatBotTools, MathTools mathTools) {
        this.ollamaChatModel = ollamaChatModel;
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

    @GetMapping("ai/user-controlled-tool-eecution")
    public List<ChatResponse> userControllerToolExecution() {
        List<ChatResponse> chatResponses = new ArrayList<>();
        ToolCallingManager toolCallingManager = DefaultToolCallingManager.builder().build();
        ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
        String conversationId = UUID.randomUUID().toString();

        ChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(ToolCallbacks.from(new MathTools()))
                .internalToolExecutionEnabled(false)
                .build();
        // add tools to prompt via ChatOptions
        Prompt prompt = new Prompt(
                List.of(new SystemMessage("You are a helpful assistant."), new UserMessage("What is 6 * 8?")),
                chatOptions);
        // add message to memory against the conversationId
        chatMemory.add(conversationId, prompt.getInstructions());
        // first message
        // re-create the prompt from the chat memory
        Prompt promptWithMemory = new Prompt(chatMemory.get(conversationId), chatOptions);
        // call llm
        ChatResponse chatResponse = ollamaChatModel.call(promptWithMemory);
        chatResponses.add(chatResponse);
        // save output to chat memory against same conversation id
        chatMemory.add(conversationId, chatResponse.getResult().getOutput());
        // if the first llm response has tool calls, execute them
        while (chatResponse.hasToolCalls()) {
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(promptWithMemory,
                    chatResponse);
            // add tool execution result to memory
            chatMemory.add(conversationId, toolExecutionResult.conversationHistory()
                    .get(toolExecutionResult.conversationHistory().size() - 1));
            // send tool result to llm
            promptWithMemory = new Prompt(chatMemory.get(conversationId), chatOptions);
            chatResponse = ollamaChatModel.call(promptWithMemory);
            // add llm response back to chat memory
            chatMemory.add(conversationId, chatResponse.getResult().getOutput());
        }

        // second message to test from memory
        UserMessage newUserMessage = new UserMessage("What did I ask you earlier?");
        chatMemory.add(conversationId, newUserMessage);

        ChatResponse newResponse = ollamaChatModel.call(new Prompt(chatMemory.get(conversationId)));
        chatResponses.add(newResponse);
        return chatResponses;
    }
}
