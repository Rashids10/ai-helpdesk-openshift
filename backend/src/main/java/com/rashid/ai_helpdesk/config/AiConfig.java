package com.rashid.ai_helpdesk.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIdType;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AiConfig {

    private static final String RAG_SYSTEM_PROMPT = """
            Du bist ein IT-FAQ-Assistent.

            Antworte ausschließlich mit Informationen aus dem bereitgestellten Kontext.
            Erfinde keine Informationen und gib keine allgemeinen Tipps oder Vermutungen.
            Wenn die Antwort nicht im Kontext steht, antworte exakt mit:
            "Keine Information gefunden. Bitte wenden Sie sich an IT-Support."
            """;

    private static final PromptTemplate RAG_USER_PROMPT = new PromptTemplate("""
            Frage:
            {query}

            Kontext:
            ---------------------
            {question_answer_context}
            ---------------------

            Beantworte die Frage direkt und knapp anhand des Kontexts.
            """);

    @Bean
    public VectorStore vectorStore(
            JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel,
            @Value("${spring.ai.vectorstore.pgvector.dimensions:1024}") int dimensions) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .schemaName("public")
                .vectorTableName("vector_store")
                .idType(PgIdType.TEXT)
                .dimensions(dimensions)
                .distanceType(PgDistanceType.COSINE_DISTANCE)
                .indexType(PgIndexType.HNSW)
                .initializeSchema(true)
                .removeExistingVectorStoreTable(false)
                .build();
    }

    @Bean
    public QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(2)
                        .similarityThreshold(0.3)
                        .build())
                .promptTemplate(RAG_USER_PROMPT)
                .build();
    }

    @Bean
    public ChatClient ragChatClient(
            OllamaChatModel chatModel,
            QuestionAnswerAdvisor questionAnswerAdvisor) {
        return ChatClient.builder(chatModel)
                .defaultSystem(RAG_SYSTEM_PROMPT)
                .defaultOptions(OllamaOptions.builder()
                        .temperature(0.0)
                        .numPredict(24)
                        .build())
                .defaultAdvisors(questionAnswerAdvisor)
                .build();
    }

}
