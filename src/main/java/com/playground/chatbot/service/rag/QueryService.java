package com.playground.chatbot.service.rag;

import com.playground.chatbot.response.RAGResponse;
import com.playground.chatbot.response.Source;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        }
        Prompt prompt = promptTemplate()
                .create(Map.of("query", payload, "question_answer_context", context));
        log.info("Final prompt: ", prompt);
        String result = Objects.requireNonNull(ollamaChatModel
                .call(prompt)
                .getResult()).getOutput().getText();

        RAGResponse ragResponse = new RAGResponse(result, sources);
        log.info("RAG Response: {}", ragResponse.response());
        return ragResponse;
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
}
