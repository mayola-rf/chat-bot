package com.playground.chatbot.controller;

import com.playground.chatbot.response.MovieRecommendation;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class GenerationBasedController {

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @GetMapping("/ai/structured-output-generation")
    List<MovieRecommendation> structuredOutput(int number, String topic) {
        StructuredOutputConverter<List<MovieRecommendation>> outputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<MovieRecommendation>>() {
        });
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template("Recomment top <number> movies on <topic>")
                .variables(Map.of("number", number, "topic", topic, "format", outputConverter.getFormat()))
                .build();

        Generation generation = this.ollamaChatModel.call(promptTemplate.create())
                .getResult();
        return outputConverter.convert(generation.getOutput().getText());
    }
}
