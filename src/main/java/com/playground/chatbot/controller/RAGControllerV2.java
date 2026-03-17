package com.playground.chatbot.controller;

import com.playground.chatbot.request.Story;
import com.playground.chatbot.response.RAGResponse;
import com.playground.chatbot.service.rag.IngestionService;
import com.playground.chatbot.service.rag.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v2/rag")
public class RAGControllerV2 {

    @Autowired
    IngestionService ingestionService;
    @Autowired
    QueryService queryService;
    @Autowired
    private PathMatcher pathMatcher;

    @PostMapping("/ingest")
    public int ingestStories(@RequestBody Story[] payload) {
        return ingestionService.ingestStories(payload);
    }

    @PostMapping("/retrieve-chunks")
    public List<String> retrieveChunks(@RequestBody String query) {
        return queryService.retrieveChunks(query);
    }

    @PostMapping("/ask")
    public String ask(@RequestBody String query) {
        return queryService.query(query);
    }

    @PostMapping("/manual-retrieval/ask")
    public RAGResponse askWithManualRetrieval(@RequestBody String query) {
        return queryService.queryWithManualRetrieval(query);
    }

    @PostMapping("/rewrite-ask")
    public RAGResponse askAfterRewritingQuery(@RequestBody String query) {
        return queryService.queryAfterRewritingPrompt(query);
    }

    @PostMapping("/hybrid-ask")
    public RAGResponse askAfterHybridRank(@RequestBody String query) {
        return queryService.askAfterHybridRank(query);
    }
}
