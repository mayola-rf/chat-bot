package com.playground.chatbot.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class ChatBotTools {

    @Tool(name = "getCurrentDateTime", description = "get my current date")
    public String getCurrentDateTime() {
        System.out.println("LLM looking for current date-time");
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    @Tool(name = "addDays", description = "Add days to a date")
    public String addDays(int day, String date) {
        System.out.println("LLM looking to add days to date");
        return LocalDateTime.parse(date).plusDays(day).toString();
    }

    @Tool(name = "findDay", description = "Find day of the week from given date")
    public String findDay(String dt) {
        System.out.println("LLM looking to add a day");
        return LocalDateTime.parse(dt).getDayOfWeek().name();
    }

    @Tool(name = "getCountry", description = "Get current country of the user")
    public String getCountry() {
        System.out.println("LLM looking for current country of the User");
        return LocaleContextHolder.getLocale().getCountry();
    }

    @Tool(name = "getPopulation", description = "Get population of any given country")
    public long getPopulation(String country) {
        System.out.println("LLM looking population of " + country);
        if (Objects.equals(country, "India")) {
            return Long.parseLong("150000000");
        } else {
            return new SecureRandom().nextLong();
        }
    }

    @Tool(name = "addTwoNumbers", description = "Add two numbers")
    public int add(int a, int b) {
        System.out.println("LLM is using this tool to calculate the sum of " + a + " and " + b);
        return a + b;
    }
}
