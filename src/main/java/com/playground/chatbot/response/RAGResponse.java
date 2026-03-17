package com.playground.chatbot.response;

import java.util.List;

public record RAGResponse(String response, List<Source> sources) {
}
