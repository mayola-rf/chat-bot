package com.playground.chatbot.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class DateTimeTools {

    @Tool(name = "getCurrentDate", description = "get current date")
    public String getCurrentDate() {
        System.out.println("LLM looking for current date-time");
        return LocalDate.now().toString();
    }

    @Tool(name = "addDays", description = "Add days to date")
    public String addDays(@ToolParam(description = "Days") int days,
                          @ToolParam(description = "Date") String date) {
        System.out.println("LLM looking to add days to date");
        return LocalDate.parse(date).plusDays(days).toString();
    }

    @Tool(name = "findDay", description = "Find day of the week from given date")
    public String findDay(String dt) {
        System.out.println("LLM looking to add a day");
        return LocalDateTime.parse(dt).getDayOfWeek().name();
    }

    @Tool(name = "getCountry", description = "Get my country")
    public String getCountry() {
        System.out.println("LLM looking for current country of the User");
        return LocaleContextHolder.getLocale().getCountry();
    }

    @Tool(name = "getPopulation", description = "Get population of country")
    public long getPopulation(String country) {
        System.out.println("LLM looking population of " + country);
        if (Objects.equals(country, "India")) {
            return Long.parseLong("150000000");
        } else {
            return new SecureRandom().nextLong();
        }
    }
}
