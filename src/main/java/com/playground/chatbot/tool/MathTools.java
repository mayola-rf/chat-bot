package com.playground.chatbot.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class MathTools {

    @Tool(name = "addTwoNumbers", description = "Add two numbers")
    public int add(int a, int b) {
        System.out.println("LLM is using this tool to calculate the sum of " + a + " and " + b);
        return a + b;
    }

    @Tool(name = "multiplyTwoNumbers", description = "Multiply two numbers")
    public int multiply(int a, int b) {
        System.out.println("LLM is using this tool to calculate the product of " + a + " and " + b);
        return a * b;
    }
}
