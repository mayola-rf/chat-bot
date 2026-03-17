package com.playground.chatbot.service.rag;

import com.playground.chatbot.request.TestCase;
import com.playground.chatbot.response.RAGResponse;
import com.playground.chatbot.response.Source;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QueryService {
    private static final Logger log = LoggerFactory.getLogger(QueryService.class);

    private final OllamaChatModel ollamaChatModel;
    private final VectorStore vectorStore;

    public QueryService(OllamaChatModel ollamaChatModel, VectorStore vectorStore) {
        this.ollamaChatModel = ollamaChatModel;
        this.vectorStore = vectorStore;
    }

    private ChatClient chatClient() {
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .topK(3)
                                .build())
                        .promptTemplate(promptTemplate())
                        .build())
                .build();
    }

    private List<Document> getDocuments(String payload) {
        log.info("retrieving similarity documents for the given payload");
        return vectorStore.similaritySearch(SearchRequest.builder()
                .topK(3) // top k relevant records
//                .similarityThreshold(0.5d) // match 0.8 and above
                // following two supports tuning.. not supported
                /*
                How MMR works (intuition)
                It selects documents iteratively:
                    pick most relevant chunk
                    next chunk =
                        high similarity to query
                        BUT low similarity to already selected chunks
                 */
//                .mmr(true)
//                .mmrLambda(0.7)
                .build());
    }

    public List<String> retrieveChunks(String payload) {
        return getDocuments(payload).stream().map(document -> document.getText()).toList();
    }

    public String query(final String payload) {
        return chatClient().prompt()
                .user(payload)
                .call()
                .content();
    }

    public RAGResponse queryWithManualRetrieval(final String payload) {
        List<Document> documents = getDocuments(payload);
        return getRagResponse(payload, documents);
    }

    private PromptTemplate promptTemplate() {
        return PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder()
                        .startDelimiterToken('<')
                        .endDelimiterToken('>')
                        .build())
                .template("""
                        You are a question-answering system.
                        
                        Use ONLY the information present in the context below.
                        Do NOT use prior knowledge.
                        Do NOT infer beyond the context.
                        If the answer is not explicitly in the context, reply exactly:
                        I don't know.
                        
                        Context:
                        <question_answer_context>
                        
                        Question:
                        <query>
                        
                        Answer with citations like [1], [2] only if those source numbers are present in the context.
                        Prefer higher-ranked sources when answering. Ignore irrelevant sources.
                        """)
                .build();
    }

    public RAGResponse queryAfterRewritingPrompt(String query) {
        String rewrittenQuery = rewriteQuery(query);
        log.info("Rewritten Query: {}", rewrittenQuery);
        return queryWithManualRetrieval(rewrittenQuery);
    }

    public RAGResponse askAfterHybridRank(String query) {
        String rewrittenQuery = rewriteQuery(query);
        List<Document> documents = getDocuments(rewrittenQuery);
        List<Document> reRanked = documents.stream().sorted(Comparator.comparingDouble(document -> getScore(query, (Document) document)).reversed())
                .toList();
        return getRagResponse(query, reRanked);
    }

    private @NonNull RAGResponse getRagResponse(String query, List<Document> documents) {
        log.info("fetched {} documents for the given payload", documents.size());
        StringBuilder context = new StringBuilder();
        List<Source> sources = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            String title = String.valueOf(document.getMetadata().getOrDefault("title", "Unknown"));
            context.append("Source : ")
                    .append(i + 1).append(", ")
                    .append("Title : ")
                    .append(title)
                    .append(", ")
                    .append("Content : ")
                    .append(document.getText())
                    .append("\n");
            sources.add(new Source(i + 1, document.getText(), title));
            log.info("Rank {} - Title: {}", i + 1, title);
            log.info("Chunk: {}", document.getText());
            log.info("Score: {}", document.getScore());
        }
        Prompt prompt = promptTemplate()
                .create(Map.of("query", query, "question_answer_context", context));
        log.info("Final prompt: ", prompt);
        String result = Objects.requireNonNull(ollamaChatModel
                .call(prompt)
                .getResult()).getOutput().getText();

        RAGResponse ragResponse = new RAGResponse(result, sources);
        log.info("RAG Response: {}", ragResponse.response());
        return ragResponse;
    }

    private double getScore(String query, Document document) {
        double normalizedVector = document.getScore() < 1 ? document.getScore() : (1.0 / (1.0 + document.getScore()));
        double keywordScore = keywordScore(query, document);
        double finalScore = 0.7 * normalizedVector + 0.3 * keywordScore;
        log.info("Final Score for document: {} is {}. before: {}", document.getMetadata().get("title"), finalScore, document.getScore());
        return finalScore;
    }

    private double keywordScore(String query, Document doc) {
        String q = query.toLowerCase();
        String title = String.valueOf(doc.getMetadata().getOrDefault("title", "")).toLowerCase();
        String text = doc.getText().toLowerCase();

        double score = 0.0;

        for (String token : q.split("\\W+")) {
            if (token.isBlank()) continue;

            if (title.contains(token)) score += 2.0;   // strong signal
            if (text.contains(token)) score += 1.0;
        }

        return score;
    }

    private String rewriteQuery(String originalQuery) {

        /*String prompt = """
        Rewrite the following user question into a concise search query that captures the core intent for semantic retrieval.

        Rules:
        - Keep it short
        - Remove unnecessary words
        - Preserve meaning
        - Do NOT answer the question

        Question:
        %s
        """.formatted(originalQuery);

        return ollamaChatModel.call(prompt).trim();*/
        Query query = new Query(originalQuery);

        QueryTransformer queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(ChatClient.builder(ollamaChatModel))
                .build();

        Query transformedQuery = queryTransformer.transform(query);
        return transformedQuery.text();
    }

    public void evaluateStoryResponses() {
        List<TestCase> testCases = new ArrayList<>();
        testCases.add(new TestCase("Who was a fool? Sheep or the Wolf? How did all the sheep got saved from the wolf?", "The Wolf in Sheep’s Clothing", 1));
        testCases.add(new TestCase("Who was a fool? Sheep or the Wolf? How did all the sheep got saved from the wolf?", "The Thirsty Crow", 3));
        testCases.add(new TestCase("What action did the crow take to drink water?", "The Thirsty Crow", 1));
        testCases.add(new TestCase("What did the crow eat?", "The Thirsty Crow", 1));

        for (TestCase testCase : testCases) {
            RAGResponse ragResponse = askAfterHybridRank(testCase.query());
            Source source = ragResponse.sources().get(testCase.rank() - 1);
            log.info("Query: {}", testCase.query());
            log.info("Expected: {}", testCase.title());
            log.info("Found: {}", source.title());
            log.info("Rank: {}", source.citationId());
        }
    }
}
