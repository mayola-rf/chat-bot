package com.playground.chatbot.service.rag;

import com.playground.chatbot.request.Story;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;

    public IngestionService(VectorStore vectorStore, TokenTextSplitter tokenTextSplitter) {
        this.vectorStore = vectorStore;
        this.tokenTextSplitter = tokenTextSplitter;
    }

    private List<Document> chunk(final Story story) {
        return tokenTextSplitter.split(Document.builder()
                .text("Title: " + story.title() + "\nContent: " + story.content())
                .metadata("storyId", story.storyId())
                .metadata("title", story.title())
                .metadata("type", story.type())
                .build());
    }

    public int ingestStories(final Story[] payload) {
        List<Document> chunked = Stream.of(payload).map(this::chunk).flatMap(List::stream).toList();
        log.info("chunked: {}", chunked.size());
        vectorStore.accept(chunked);
        log.debug("vector store populated");
        // List<Document> retrieved = vectorStore.similaritySearch("tell me a story"); return retrieved.size();
        return chunked.size();
    }
}
